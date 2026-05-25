const ok = (res, data = null, message = 'OK', meta = undefined) => {
  const payload = { success: true, message, data };
  if (meta) payload.meta = meta;
  return res.status(200).json(payload);
};

const created = (res, data, message = 'Created') =>
  res.status(201).json({ success: true, message, data });

const noContent = (res) => res.status(204).send();

module.exports = { ok, created, noContent };
