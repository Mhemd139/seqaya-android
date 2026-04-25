-- One-time cleanup of seqaya.io@gmail.com test fleet.
-- Run BEFORE 2026-04-25-cascade-delete-readings.sql.
--
-- Keeps:
--   1, 2, 9 (firmware test bench)
--   SQ-EDD6FE91 (Peace Lily — current dogfood device)
--
-- Removes:
--   SQ-92E72B6C (Lucy mock) and any other rows under seqaya.io@gmail.com
--   that aren't in the keep list.
--   Plus orphan readings whose device row is gone.

BEGIN;

-- 1. Delete readings for the to-be-deleted devices (cascade isn't in place yet).
DELETE FROM device_readings
WHERE device_serial IN (
    SELECT serial FROM devices
    WHERE owner_id = (SELECT id FROM auth.users WHERE email = 'seqaya.io@gmail.com')
      AND serial NOT IN ('1', '2', '9', 'SQ-EDD6FE91')
);

-- 2. Delete the device rows themselves.
DELETE FROM devices
WHERE owner_id = (SELECT id FROM auth.users WHERE email = 'seqaya.io@gmail.com')
  AND serial NOT IN ('1', '2', '9', 'SQ-EDD6FE91');

-- 3. Delete any orphan readings (no matching device row at all — leftovers
--    from previous deletes that didn't cascade).
DELETE FROM device_readings r
WHERE NOT EXISTS (SELECT 1 FROM devices d WHERE d.serial = r.device_serial);

COMMIT;
