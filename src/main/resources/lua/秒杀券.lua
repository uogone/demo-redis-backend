local userId = ARGV[1]
local voucherKey = ARGV[2]
local orderKey = ARGV[3]

-- 检查库存
if(tonumber(redis.call('hget', voucherKey, 'stock')) <= 0) then
    return 1
end

-- 检查一人一单
if(redis.call('sismember', orderKey, userId) == 1) then
    return 2
end

-- 减库存
redis.call('hincrby', voucherKey, 'stock', -1)

-- 保存用户id
redis.call('sadd', orderKey, userId)

return 0