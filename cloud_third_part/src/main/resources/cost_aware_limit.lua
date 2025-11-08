local key = KEYS[1]
local limit = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local coolDown = tonumber(ARGV[3])
local now = tonumber(ARGV[4])
local costLevel = ARGV[5]

-- 检查冷却期
local coolKey = key .. ':cool'
local coolUntil = redis.call('GET', coolKey)
if coolUntil and tonumber(coolUntil) > now then
    return 0
end

-- 滑动窗口计数
local clearTime = now - window
redis.call('ZREMRANGEBYSCORE', key, 0, clearTime)

local current = redis.call('ZCARD', key)

-- 根据成本等级动态调整限制
local adjustedLimit = limit
if costLevel == 'HIGH' then
    adjustedLimit = math.floor(limit * 0.7)
elseif costLevel == 'CRITICAL' then
    adjustedLimit = math.floor(limit * 0.5)
end

if current >= adjustedLimit then
    -- 触发限制，进入冷却期
    redis.call('SETEX', coolKey, coolDown, tostring(now + coolDown * 1000))
    return 0
end

-- 记录本次调用
redis.call('ZADD', key, now, now)
redis.call('EXPIRE', key, math.floor(window / 1000) + coolDown)

return 1