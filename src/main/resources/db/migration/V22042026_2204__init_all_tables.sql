CREATE TABLE users
(
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name           VARCHAR(100) NOT NULL,
    surname        VARCHAR(100) NOT NULL,
    login          VARCHAR(50)  NOT NULL UNIQUE,
    email          VARCHAR(255) NOT NULL UNIQUE,
    phone          VARCHAR(12) CHECK (phone ~ '^[0-9]{12}$'),
    role           VARCHAR(20)      DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN')),
    hashedPassword VARCHAR(255) NOT NULL,
    created_at     TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_login ON users (login);

CREATE TABLE lottery_draws
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    draw_number   INTEGER NOT NULL UNIQUE GENERATED ALWAYS AS IDENTITY,
    draw_name     VARCHAR(100) DEFAULT null,
    total_tickets INTEGER          DEFAULT 0,
    status        VARCHAR(20)      DEFAULT 'SCHEDULED' CHECK (status IN ('SCHEDULED', 'ACTIVE', 'COMPLETED', 'CANCELLED')),
    created_at    TIMESTAMP        DEFAULT current_timestamp
);

CREATE INDEX idx_draws_status ON lottery_draws (status);
CREATE INDEX idx_draws_name ON lottery_draws (draw_name);
CREATE INDEX idx_draws_number ON lottery_draws (draw_number);

CREATE TABLE lottery_tickets
(
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID       NOT NULL,
    lottery_draw_id UUID       NOT NULL,
    status          VARCHAR(20)      default 'PENDING' CHECK ( status IN ('PENDING', 'WIN', 'LOSE') ),
    ticket_numbers  INTEGER[5] NOT NULL,
    created_at      TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_lottery_draw_id FOREIGN KEY (lottery_draw_id) REFERENCES lottery_draws (id) ON DELETE CASCADE,

    CONSTRAINT check_array_length CHECK (array_length(ticket_numbers, 1) = 5),

    CONSTRAINT check_numbers_range CHECK (
        1 <= ALL (ticket_numbers) AND 45 >= ALL (ticket_numbers)
        ),

    CONSTRAINT check_unique_numbers CHECK (
        ticket_numbers[1] != ticket_numbers[2] AND
        ticket_numbers[1] != ticket_numbers[3] AND
        ticket_numbers[1] != ticket_numbers[4] AND
        ticket_numbers[1] != ticket_numbers[5] AND
        ticket_numbers[2] != ticket_numbers[3] AND
        ticket_numbers[2] != ticket_numbers[4] AND
        ticket_numbers[2] != ticket_numbers[5] AND
        ticket_numbers[3] != ticket_numbers[4] AND
        ticket_numbers[3] != ticket_numbers[5] AND
        ticket_numbers[4] != ticket_numbers[5]
)
    );

CREATE INDEX idx_tickets_user ON lottery_tickets (user_id);
CREATE INDEX idx_tickets_lottery_draw ON lottery_tickets (lottery_draw_id);
CREATE INDEX idx_tickets_status ON lottery_tickets (status);

CREATE OR REPLACE FUNCTION generate_lottery_numbers()
    RETURNS INTEGER[] AS
$$
DECLARE
result   INTEGER[] := ARRAY []::INTEGER[];
    next_num INTEGER;
BEGIN
    WHILE COALESCE(array_length(result, 1), 0) < 5
        LOOP
            next_num := floor(random() * 45 + 1)::INTEGER;

            IF NOT next_num = ANY (result) THEN
                result := array_append(result, next_num);
END IF;
END LOOP;

RETURN result;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE lottery_draws_result
(
    id              UUID PRIMARY KEY    DEFAULT gen_random_uuid(),
    result_numbers  INTEGER[5] NOT NULL default generate_lottery_numbers(),
    created_at      TIMESTAMP           DEFAULT CURRENT_TIMESTAMP,
    lottery_draw_id UUID       NOT NULL unique,

    CONSTRAINT fk_lottery_draws_id FOREIGN KEY (lottery_draw_id) REFERENCES lottery_draws (id) ON DELETE RESTRICT,

    CONSTRAINT check_array_length CHECK (array_length(result_numbers, 1) = 5),

    CONSTRAINT check_numbers_range CHECK (
        1 <= ALL (result_numbers) AND 45 >= ALL (result_numbers)
        ),

    CONSTRAINT check_unique_numbers CHECK (
        result_numbers[1] != result_numbers[2] AND
        result_numbers[1] != result_numbers[3] AND
        result_numbers[1] != result_numbers[4] AND
        result_numbers[1] != result_numbers[5] AND
        result_numbers[2] != result_numbers[3] AND
        result_numbers[2] != result_numbers[4] AND
        result_numbers[2] != result_numbers[5] AND
        result_numbers[3] != result_numbers[4] AND
        result_numbers[3] != result_numbers[5] AND
        result_numbers[4] != result_numbers[5]
)
    );

CREATE INDEX idx_lottery_draws_id ON lottery_draws_result (lottery_draw_id);

