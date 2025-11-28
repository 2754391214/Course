-- 选课操作的Lua脚本（Set结构）
-- KEYS[1]: 课程当前人数key (course:current:{courseId})
-- KEYS[2]: 学生选课Set key (student:courses:{studentId})
-- ARGV[1]: 课程容量
-- ARGV[2]: 课程ID
-- ARGV[3]: 过期时间（秒）
-- 返回值: 0-成功, 1-已选过, 2-课程已满, 3-系统异常

local currentKey = KEYS[1]
local studentSetKey = KEYS[2]
local capacity = tonumber(ARGV[1])
local courseId = ARGV[2]
local expireTime = tonumber(ARGV[3])

-- 检查学生是否已经选过该课程（Set检查）
if redis.call('SISMEMBER', studentSetKey, courseId) == 1 then
    return 1
end

-- 检查课程是否已满
local currentCount = tonumber(redis.call('GET', currentKey) or 0)
if currentCount >= capacity then
    return 2
end

-- 原子性操作：增加人数、添加选课记录、设置过期时间
redis.call('INCR', currentKey)
redis.call('SADD', studentSetKey, courseId)

-- 设置Set的过期时间
redis.call('EXPIRE', studentSetKey, expireTime)

return 0