-- 改进的退课Lua脚本
-- KEYS[1]: 课程当前人数key (course:current:{courseId})
-- KEYS[2]: 学生选课标记key (student:enrollment:{studentId}:{courseId})
-- 返回值: 0-退课成功, 1-Redis中未找到选课记录（需要检查数据库）, 2-系统异常

local currentKey = KEYS[1]
local studentKey = KEYS[2]

-- 减少当前选课人数（确保不小于0）
local currentCount = tonumber(redis.call('GET', currentKey) or 0)
if currentCount > 0 then
    redis.call('DECR', currentKey)
end

-- 删除学生选课标记（如果存在）
if redis.call('EXISTS', studentKey) == 1 then
    redis.call('DEL', studentKey)
    return 0  -- Redis中找到并删除标记
else
    return 1  -- Redis中未找到选课标记
end