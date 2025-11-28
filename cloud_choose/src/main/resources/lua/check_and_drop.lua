-- 改进的退课Lua脚本（Set结构适配版）
-- KEYS[1]: 课程当前人数key (course:current:{courseId})
-- KEYS[2]: 学生选课Set key (student:courses:{studentId})
-- ARGV[1]: 课程ID
-- ARGV[2]: 过期时间（秒）
-- 返回值: 0-退课成功, 1-Redis中未找到选课记录（需要检查数据库）, 2-系统异常

local currentKey = KEYS[1]
local studentSetKey = KEYS[2]
local courseId = ARGV[1]
local expireTime = tonumber(ARGV[2])

-- 减少当前选课人数（确保不小于0）
local currentCount = tonumber(redis.call('GET', currentKey) or 0)
if currentCount > 0 then
    redis.call('DECR', currentKey)
end

-- 从学生选课Set中移除课程ID
local removedCount = redis.call('SREM', studentSetKey, courseId)

if removedCount > 0 then
    -- 如果Set不为空，更新过期时间；如果为空，删除整个Set
    local setSize = redis.call('SCARD', studentSetKey)
    if setSize > 0 then
        redis.call('EXPIRE', studentSetKey, expireTime)
    else
        redis.call('DEL', studentSetKey)
    end
    return 0  -- Redis中找到并删除选课记录
else
    return 1  -- Redis中未找到选课记录
end