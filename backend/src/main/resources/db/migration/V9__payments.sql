CREATE TABLE seller_payout_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    razorpay_account_id VARCHAR(40) NOT NULL,
    razorpay_product_id VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    listing_id UUID NOT NULL REFERENCES listings (id) ON DELETE CASCADE,
    buyer_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    seller_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    razorpay_order_id VARCHAR(40) NOT NULL UNIQUE,
    razorpay_payment_id VARCHAR(40),
    amount NUMERIC(10, 2) NOT NULL,
    platform_fee_amount NUMERIC(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_payments_listing_id ON payments (listing_id);
CREATE INDEX idx_payments_buyer_id ON payments (buyer_id);
CREATE INDEX idx_payments_seller_id ON payments (seller_id);
CREATE INDEX idx_payments_status ON payments (status);
