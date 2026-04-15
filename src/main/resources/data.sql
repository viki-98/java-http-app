INSERT INTO test_users (name) VALUES ('Alice')    ON CONFLICT DO NOTHING;
INSERT INTO test_users (name) VALUES ('Bob')      ON CONFLICT DO NOTHING;
INSERT INTO test_users (name) VALUES ('Charlie')  ON CONFLICT DO NOTHING;
