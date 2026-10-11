-- Zones table
CREATE TABLE IF NOT EXISTS zones (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  region TEXT NOT NULL,
  coverage_description TEXT
);

-- Time blocks table
CREATE TABLE IF NOT EXISTS time_blocks (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  zone_id INTEGER NOT NULL,
  available_date TEXT NOT NULL,  -- ISO date: YYYY-MM-DD
  start_time TEXT NOT NULL,      -- HH:MM
  end_time TEXT NOT NULL,        -- HH:MM
  capacity INTEGER NOT NULL DEFAULT 10,
  reserved_count INTEGER NOT NULL DEFAULT 0,
  cost REAL NOT NULL DEFAULT 0.0,
  FOREIGN KEY (zone_id) REFERENCES zones(id)
);

-- Reservations table
CREATE TABLE IF NOT EXISTS reservations (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  order_id TEXT NOT NULL UNIQUE,
  time_block_id INTEGER NOT NULL,
  customer_name TEXT NOT NULL,
  address TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'CONFIRMED',
  created_at TEXT NOT NULL,
  FOREIGN KEY (time_block_id) REFERENCES time_blocks(id)
);

-- Seed zones (INSERT OR IGNORE uses id uniqueness to avoid duplicates)
INSERT OR IGNORE INTO zones (id, name, region, coverage_description) VALUES
  (1, 'Metropolitana Norte', 'RM Norte', 'Comunas: Huechuraba, Quilicura, Recoleta, Independencia'),
  (2, 'Metropolitana Centro', 'RM Centro', 'Comunas: Santiago, Providencia, Nunoa, Macul'),
  (3, 'Metropolitana Sur', 'RM Sur', 'Comunas: La Florida, Puente Alto, San Bernardo, Buin'),
  (4, 'Metropolitana Oriente', 'RM Oriente', 'Comunas: Las Condes, Vitacura, Lo Barnechea, La Reina'),
  (5, 'Los Ríos', 'Lor Ríos', 'Comunas: Lanco, Panguipulli, Valdivia, San José, Máfil');
