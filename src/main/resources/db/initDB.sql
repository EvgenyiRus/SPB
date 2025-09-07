-- Таблица для мужчин
CREATE TABLE mans (
                                 id BIGSERIAL PRIMARY KEY,
                                 birth_date DATE NOT NULL,
                                 region INTEGER NOT NULL,
                                 income NUMERIC(10, 2),
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица для женщин
CREATE TABLE womens (
                                   id BIGSERIAL PRIMARY KEY,
                                   birth_date DATE NOT NULL,
                                   region INTEGER NOT NULL,
                                   income NUMERIC(10, 2),
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- Таблица статистики по регионам
CREATE TABLE region_statistics (
                                   region INTEGER PRIMARY KEY,
                                   total_population BIGINT DEFAULT 0,
                                   working_age_population BIGINT DEFAULT 0,
                                   working_age_percentage DECIMAL(5,2) DEFAULT 0,
                                   average_income NUMERIC(10, 2) DEFAULT 0,
                                   max_income NUMERIC(10, 2) DEFAULT 0,
                                   unemployed_count BIGINT DEFAULT 0,
                                   unemployed_percentage DECIMAL(5,2) DEFAULT 0,
                                   last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица общей статистики
CREATE TABLE total_statistics (
                                  id INTEGER PRIMARY KEY DEFAULT 1,
                                  total_population BIGINT DEFAULT 0,
                                  working_age_population BIGINT DEFAULT 0,
                                  working_age_percentage DECIMAL(5,2) DEFAULT 0,
                                  average_income NUMERIC(10, 2) DEFAULT 0,
                                  max_income NUMERIC(10, 2) DEFAULT 0,
                                  unemployed_count BIGINT DEFAULT 0,
                                  unemployed_percentage DECIMAL(5,2) DEFAULT 0,
                                  last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);