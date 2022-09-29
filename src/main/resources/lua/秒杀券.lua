local userId = ARGV[1]
local voucherId = ARGV[2]
local orderId = ARGV[3]

-- 检查库存
if(tonumber(redis.call('hget', 'seckill:info:'..voucherId, 'stock')) <= 0) then
    return 1
end

-- 检查一人一单
if(redis.call('sismember', 'seckill:user:'..voucherId, userId) == 1) then
    return 2
end

-- 减库存
redis.call('hincrby', 'seckill:info:'..voucherId, 'stock', -1)

-- 保存用户id
redis.call('sadd', 'seckill:user:'..voucherId, userId)

-- 发送订单消息
redis.call('xadd', 'orders', '*', 'userId', userId, 'voucherId', voucherId, 'id', orderId)

return 0