local key = KEYS[1]
local dailyBudget = tonumber(ARGV[1])
local cost = tonumber(ARGV[2])
local alertThreshold = tonumber(ARGV[3])

local currentCost = redis.call('GET', key)
if not currentCost then
    currentCost = 0
else
    currentCost = tonumber(currentCost)
end

local newCost = currentCost + cost

if newCost > dailyBudget then
    return 0
end

-- 检查告警阈值
if currentCost <= dailyBudget * alertThreshold and newCost > dailyBudget * alertThreshold then
    -- 触发阈值告警（由Java层处理）
end

redis.call('SET', key, tostring(newCost))
return 1