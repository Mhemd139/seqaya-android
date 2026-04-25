-- Add ON DELETE CASCADE to device_readings.device_serial → devices.serial.
--
-- After this runs, deleting a device row also deletes its readings, so:
--   - Grafana panels stop showing data for deleted devices
--   - Test cycle (add → calibrate → delete → repeat) doesn't accumulate junk
--   - App-side delete in DeviceRepository can stop double-deleting (defensive
--     code there is still kept as belt-and-suspenders for offline scenarios)
--
-- Run AFTER 2026-04-25-cleanup-test-devices.sql so the cascade rebuild doesn't
-- have to walk thousands of stale rows.

BEGIN;

-- Drop the existing FK (name comes from the original migration; if your
-- schema diverged, find the actual name with:
--   SELECT conname FROM pg_constraint
--   WHERE conrelid = 'device_readings'::regclass AND contype = 'f'
--     AND conname LIKE '%device_serial%';
-- and replace below).

ALTER TABLE device_readings
    DROP CONSTRAINT IF EXISTS device_readings_device_serial_fkey;

-- Re-add it with cascade.

ALTER TABLE device_readings
    ADD CONSTRAINT device_readings_device_serial_fkey
    FOREIGN KEY (device_serial)
    REFERENCES devices(serial)
    ON DELETE CASCADE;

COMMIT;
