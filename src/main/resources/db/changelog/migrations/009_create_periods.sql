CREATE TABLE periods(
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    academic_year VARCHAR(9) NOT NULL,
    CONSTRAINT uniq_periods_name_academic_year UNIQUE (name, academic_year),
    CONSTRAINT chk_periods_dates CHECK (start_date < end_date)
);