const router = require('express').Router();
const validate = require('../../middleware/validate');
const { requireAuth, requireRole } = require('../../middleware/auth');
const v = require('./room.validators');
const c = require('./room.controller');

router.use(requireAuth, requireRole('admin'));

router.get('/', validate(v.listRoomsSchema, 'query'), c.list);
router.post('/', validate(v.createRoomSchema), c.createRoom);

router.get('/:id', c.get);
router.patch('/:id', validate(v.updateRoomSchema), c.updateRoom);
router.delete('/:id', c.removeRoom);

router.post('/:id/beds', validate(v.addBedSchema), c.addBed);
router.delete('/:id/beds/:bedId', c.removeBed);

module.exports = router;
