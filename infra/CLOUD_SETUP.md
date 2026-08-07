# Cloud setup runbook (one-time)

These commands provision the account-side resources the codebase in this repo assumes exist.
Run them yourself (they mutate IAM/billing-adjacent state) — this file is the reference, not
something meant to be executed unattended. Replace `OWNER/REPO` with your GitHub `owner/repo`.

Project: `e-commerce-bb89d` · Region: `us-central1`

```bash
gcloud config set project e-commerce-bb89d
```

## 1. Enable required APIs

```bash
gcloud services enable \
  run.googleapis.com \
  artifactregistry.googleapis.com \
  pubsub.googleapis.com \
  secretmanager.googleapis.com \
  iam.googleapis.com \
  iamcredentials.googleapis.com \
  cloudresourcemanager.googleapis.com
```

## 2. Artifact Registry

```bash
gcloud artifacts repositories create ecommerce \
  --repository-format=docker \
  --location=us-central1
```

## 3. Cloud Run runtime service account

This is the identity the *container* runs as — separate from the identity GitHub Actions
deploys as. Scoped to only what the app needs at runtime.

```bash
gcloud iam service-accounts create ecommerce-backend-runtime \
  --display-name="ecommerce backend runtime"

# Pub/Sub
gcloud projects add-iam-policy-binding e-commerce-bb89d \
  --member="serviceAccount:ecommerce-backend-runtime@e-commerce-bb89d.iam.gserviceaccount.com" \
  --role="roles/pubsub.editor"

# Firebase Storage bucket (grant on the bucket, not project-wide)
gcloud storage buckets add-iam-policy-binding gs://e-commerce-bb89d.firebasestorage.app \
  --member="serviceAccount:ecommerce-backend-runtime@e-commerce-bb89d.iam.gserviceaccount.com" \
  --role="roles/storage.objectAdmin"
```

(Secret Manager access for this SA is granted in step 5, after the secrets exist.)

Set this as a GitHub Actions **repo variable** named `CLOUD_RUN_RUNTIME_SA` =
`ecommerce-backend-runtime@e-commerce-bb89d.iam.gserviceaccount.com` — `deploy.yml` passes it
to `gcloud run deploy --service-account`.

## 4. GitHub Actions deploy identity (Workload Identity Federation, keyless)

```bash
gcloud iam service-accounts create github-deployer \
  --display-name="GitHub Actions deployer"

DEPLOY_SA="github-deployer@e-commerce-bb89d.iam.gserviceaccount.com"

gcloud projects add-iam-policy-binding e-commerce-bb89d \
  --member="serviceAccount:$DEPLOY_SA" --role="roles/run.admin"
gcloud projects add-iam-policy-binding e-commerce-bb89d \
  --member="serviceAccount:$DEPLOY_SA" --role="roles/artifactregistry.writer"
gcloud projects add-iam-policy-binding e-commerce-bb89d \
  --member="serviceAccount:$DEPLOY_SA" --role="roles/iam.serviceAccountUser"
gcloud projects add-iam-policy-binding e-commerce-bb89d \
  --member="serviceAccount:$DEPLOY_SA" --role="roles/firebasehosting.admin"

gcloud iam workload-identity-pools create github-pool \
  --location=global --display-name="GitHub Actions"

gcloud iam workload-identity-pools providers create-oidc github-provider \
  --location=global \
  --workload-identity-pool=github-pool \
  --display-name="GitHub provider" \
  --issuer-uri="https://token.actions.githubusercontent.com" \
  --attribute-mapping="google.subject=assertion.sub,attribute.repository=assertion.repository" \
  --attribute-condition="assertion.repository=='OWNER/REPO'"

PROJECT_NUMBER=$(gcloud projects describe e-commerce-bb89d --format="value(projectNumber)")

gcloud iam service-accounts add-iam-policy-binding "$DEPLOY_SA" \
  --role="roles/iam.workloadIdentityUser" \
  --member="principalSet://iam.googleapis.com/projects/$PROJECT_NUMBER/locations/global/workloadIdentityPools/github-pool/attribute.repository/OWNER/REPO"
```

Set two GitHub Actions **repo secrets**:
- `GCP_SERVICE_ACCOUNT_EMAIL` = `github-deployer@e-commerce-bb89d.iam.gserviceaccount.com`
- `GCP_WORKLOAD_IDENTITY_PROVIDER` = `projects/$PROJECT_NUMBER/locations/global/workloadIdentityPools/github-pool/providers/github-provider`

## 5. Secret Manager (DB + Redis passwords)

```bash
gcloud secrets create supabase-db-password --replication-policy=automatic
gcloud secrets create upstash-redis-password --replication-policy=automatic

# paste the actual password when prompted — don't put it inline in shell history
gcloud secrets versions add supabase-db-password --data-file=-
gcloud secrets versions add upstash-redis-password --data-file=-

for s in supabase-db-password upstash-redis-password; do
  gcloud secrets add-iam-policy-binding "$s" \
    --member="serviceAccount:ecommerce-backend-runtime@e-commerce-bb89d.iam.gserviceaccount.com" \
    --role="roles/secretmanager.secretAccessor"
done
```

## 5b. Secret Manager (Razorpay)

Same pattern as step 5 — three secrets for Route payments. Get real Test Mode values from your
Razorpay dashboard (Settings → API Keys, and Settings → Webhooks for the webhook secret once
you've configured the webhook URL in step 7's follow-up).

```bash
gcloud secrets create razorpay-key-id --replication-policy=automatic
gcloud secrets create razorpay-key-secret --replication-policy=automatic
gcloud secrets create razorpay-webhook-secret --replication-policy=automatic

gcloud secrets versions add razorpay-key-id --data-file=-
gcloud secrets versions add razorpay-key-secret --data-file=-
gcloud secrets versions add razorpay-webhook-secret --data-file=-

for s in razorpay-key-id razorpay-key-secret razorpay-webhook-secret; do
  gcloud secrets add-iam-policy-binding "$s" \
    --member="serviceAccount:ecommerce-backend-runtime@e-commerce-bb89d.iam.gserviceaccount.com" \
    --role="roles/secretmanager.secretAccessor"
done
```

In the Razorpay dashboard, point the webhook at
`https://<your-cloud-run-url>/api/webhooks/razorpay`, subscribed to at least `payment.captured`
and `payment.failed` — the secret you set there must match `razorpay-webhook-secret` above.

## 6. Firebase Hosting sites

Requires the Firebase CLI logged into an account with access to `e-commerce-bb89d`
(if you hit the account-mismatch issue, run `firebase login --reauth` first).

```bash
firebase hosting:sites:create ecommerce-bb89d-storefront --project=e-commerce-bb89d
firebase hosting:sites:create ecommerce-bb89d-admin --project=e-commerce-bb89d

firebase target:apply hosting storefront ecommerce-bb89d-storefront --project=e-commerce-bb89d
firebase target:apply hosting admin ecommerce-bb89d-admin --project=e-commerce-bb89d
```

(These site IDs must be globally unique across all Firebase projects — if either name is
taken, pick another and update `.firebaserc` to match.)

## 7. GitHub repo configuration

Push the repo to GitHub first if you haven't (`git remote add origin ...`, `git push -u origin main`),
then set, via repo Settings → Secrets and variables → Actions:

**Secrets:**
| Name | Value |
|---|---|
| `GCP_WORKLOAD_IDENTITY_PROVIDER` | from step 4 |
| `GCP_SERVICE_ACCOUNT_EMAIL` | from step 4 |

**Variables:**
| Name | Value |
|---|---|
| `CLOUD_RUN_RUNTIME_SA` | `ecommerce-backend-runtime@e-commerce-bb89d.iam.gserviceaccount.com` |
| `SUPABASE_DB_HOST` | your Supabase project's `db.<ref>.supabase.co` host |
| `SUPABASE_DB_USER` | usually `postgres` |
| `UPSTASH_REDIS_HOST` | your Upstash Redis endpoint |
| `UPSTASH_REDIS_PORT` | usually `6379` |

The actual DB/Redis *passwords* are never GitHub secrets — they live only in Secret Manager
(step 5) and are injected into Cloud Run at deploy time via `--set-secrets`.

## Verifying

```bash
gcloud run services describe ecommerce-backend --region=us-central1 --format="value(status.url)"
firebase hosting:sites:list --project=e-commerce-bb89d
```
