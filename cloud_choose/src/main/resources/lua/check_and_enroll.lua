-- 防止超选的Lua脚本（完整版）
-- KEYS[1]: 课程当前人数key (course:current:{courseId})
-- KEYS[2]: 学生选课标记key (student:enrollment:{studentId}:{courseId})
-- ARGV[1]: 课程容量
-- 返回值: 0-成功, 1-已选过, 2-课程已满, 3-系统异常

local currentKey = KEYS[1]
local studentKey = KEYS[2]
local capacity = tonumber(ARGV[1])

-- 检查学生是否已经选过该课程
if redis.call('EXISTS', studentKey) == 1 then
    return 1
end

-- 获取当前选课人数
local currentCount = tonumber(redis.call('GET', currentKey) or 0)

-- 检查是否超过容量
if currentCount >= capacity then
    return 2
end

-- 原子性增加人数并设置学生选课标记
redis.call('INCR', currentKey)
redis.call('SET', studentKey, '1', 'EX', 86400)

return 0