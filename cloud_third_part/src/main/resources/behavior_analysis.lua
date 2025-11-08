local key = KEYS[1]
local limit = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local coolDown = tonumber(ARGV[3])
local callerId = ARGV[4]
local now = tonumber(ARGV[5])
local features = ARGV[6]

-- 分析调用模式
local patternKey = key .. ':pattern'
local currentPattern = redis.call('HGET', patternKey, features)

if currentPattern then
    local patternData = cjson.decode(currentPattern)
    patternData.count = patternData.count + 1
    patternData.lastSeen = now

    -- 检测异常模式（短时间内相同特征频繁调用）
    if patternData.count > limit and (now - patternData.firstSeen) < window then
        redis.call('SETEX', key .. ':cool', coolDown, '1')
        return 0
    end

    redis.call('HSET', patternKey, features, cjson.encode(patternData))
else
    local newPattern = {
        count = 1,
        firstSeen = now,
        lastSeen = now
    }
    redis.call('HSET', patternKey, features, cjson.encode(newPattern))
end

redis.call('EXPIRE', patternKey, 86400) -- 模式数据保留24小时

return 1