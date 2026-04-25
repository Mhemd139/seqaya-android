-- One-time cleanup of seqaya.io@gmail.com test fleet.
-- Run BEFORE 2026-04-25-cascade-delete-readings.sql so we don't have to wait
-- for cascade behavior — this script deletes readings explicitly first.
--
-- Keeps:
--   1, 2, 9 (production fleet on the firmware test bench)
--   SQ-EDD6FE91 (Peace Lily — current Phase 6 dogfood device)
--
-- Removes:
--   SQ-92E72B6C (Lucy mock — debug-only seed)
--   Orphan readings for any serial without a corresponding devices row
--   (earlier audit found SQ-2F05602D, SQ-A2D9C075 — re-checked dynamically below)

BEGIN;

-- 1. Confirm what we'll delete BEFORE deleting (dry-run preview).
--    Comment out the COMMIT below and run only this section first if you want
--    to eyeball the count before pulling the trigger.

DO $$
DECLARE
    devices_to_delete int;
    orphan_readings int;
    keepers text[] := ARRAY['1', '2', '9', 'SQ-EDD6FE91'];
    owner_uid uuid;
BEGIN
    SELECT id INTO owner_uid FROM auth.users WHERE email = 'seqaya.io@gmail.com';
    IF owner_uid IS NULL THEN
        RAISE EXCEPTION 'Owner seqaya.io@gmail.com not found';
    END IF;

    SELECT count(*) INTO devices_to_delete
    FROM devices
    WHERE owner_id = owner_uid AND serial NOT IN (SELECT unnest(keepers));

    SELECT count(*) INTO orphan_readings
    FROM device_readings r
    WHERE NOT EXISTS (SELECT 1 FROM devices d WHERE d.serial = r.device_serial);

    RAISE NOTICE 'Will delete % device rows and % orphan reading rows',
        devices_to_delete, orphan_readings;
END $$;

-- 2. Delete readings for the to-be-deleted devices (because cascade isn't in place yet).

DELETE FROM device_readings
WHERE device_serial IN (
    SELECT serial FROM devices
    WHERE owner_id = (SELECT id FROM auth.users WHERE email = 'seqaya.io@gmail.com')
      AND serial NOT IN ('1', '2', '9', 'SQ-EDD6FE91')
);

-- 3. Delete the device rows themselves.

DELETE FROM devices
WHERE owner_id = (SELECT id FROM auth.users WHERE email = 'seqaya.io@gmail.com')
  AND serial NOT IN ('1', '2', '9', 'SQ-EDD6FE91');

-- 4. Delete any orphan readings (no matching device row at all — leftovers
--    from previous deletes that didn't cascade).

DELETE FROM device_readings r
WHERE NOT EXISTS (SELECT 1 FROM devices d WHERE d.serial = r.device_serial);

COMMIT;
