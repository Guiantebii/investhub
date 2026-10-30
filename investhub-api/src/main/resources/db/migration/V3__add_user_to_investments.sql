-- Dados legados (pré multi-tenant) não têm dono; remove antes de exigir user_id.
DELETE FROM transactions;
DELETE FROM assets;

ALTER TABLE assets DROP CONSTRAINT uk_assets_symbol;

ALTER TABLE assets ADD COLUMN user_id BIGINT NOT NULL;
ALTER TABLE assets ADD CONSTRAINT fk_assets_user FOREIGN KEY (user_id) REFERENCES users (id);
ALTER TABLE assets ADD CONSTRAINT uk_assets_user_symbol UNIQUE (user_id, symbol);

ALTER TABLE transactions ADD COLUMN user_id BIGINT NOT NULL;
ALTER TABLE transactions ADD CONSTRAINT fk_transactions_user FOREIGN KEY (user_id) REFERENCES users (id);

CREATE INDEX idx_assets_user_id ON assets (user_id);
CREATE INDEX idx_transactions_user_id ON transactions (user_id);
