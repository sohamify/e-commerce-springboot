CREATE TABLE listings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title VARCHAR(140) NOT NULL,
    description TEXT NOT NULL,
    price NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    condition VARCHAR(20) NOT NULL,
    category VARCHAR(20) NOT NULL,
    location VARCHAR(120),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    buyer_id UUID REFERENCES users (id) ON DELETE SET NULL,
    sold_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    search_vector tsvector GENERATED ALWAYS AS (
        setweight(to_tsvector('english', coalesce(title, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(description, '')), 'B')
    ) STORED
);

CREATE INDEX idx_listings_seller_id ON listings (seller_id);
CREATE INDEX idx_listings_buyer_id ON listings (buyer_id);
CREATE INDEX idx_listings_status ON listings (status);
CREATE INDEX idx_listings_category ON listings (category);
CREATE INDEX idx_listings_search_vector ON listings USING GIN (search_vector);

CREATE TABLE listing_photos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    listing_id UUID NOT NULL REFERENCES listings (id) ON DELETE CASCADE,
    url VARCHAR(500) NOT NULL,
    position SMALLINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_listing_photos_listing_id ON listing_photos (listing_id);

CREATE TABLE listing_tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    listing_id UUID NOT NULL REFERENCES listings (id) ON DELETE CASCADE,
    tag VARCHAR(40) NOT NULL,
    UNIQUE (listing_id, tag)
);

CREATE INDEX idx_listing_tags_tag ON listing_tags (tag);

CREATE TABLE ratings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    listing_id UUID NOT NULL REFERENCES listings (id) ON DELETE CASCADE,
    rater_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    ratee_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    score SMALLINT NOT NULL CHECK (score BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (listing_id, rater_id)
);

CREATE INDEX idx_ratings_ratee_id ON ratings (ratee_id);

CREATE TABLE reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    reported_listing_id UUID REFERENCES listings (id) ON DELETE CASCADE,
    reported_user_id UUID REFERENCES users (id) ON DELETE CASCADE,
    reason TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    resolved_by UUID REFERENCES users (id),
    resolved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CHECK (reported_listing_id IS NOT NULL OR reported_user_id IS NOT NULL)
);

CREATE INDEX idx_reports_status ON reports (status);
