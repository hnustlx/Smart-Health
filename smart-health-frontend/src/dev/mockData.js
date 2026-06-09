const MOCK_DELAY = 180

let weightRecords = [
  { id: 101, recordDate: '2026-05-28', weight: 73.6 },
  { id: 102, recordDate: '2026-05-31', weight: 73.1 },
  { id: 103, recordDate: '2026-06-03', weight: 72.8 },
  { id: 104, recordDate: '2026-06-06', weight: 72.2 },
  { id: 105, recordDate: '2026-06-08', weight: 71.9 }
]

let users = [
  { id: 1, username: 'admin_preview', role: 'ADMIN', status: 1, vipExpireTime: '2027-01-01 00:00:00' },
  { id: 2, username: 'lin_xiaoyu', role: 'VIP', status: 1, vipExpireTime: '2026-12-31 23:59:59' },
  { id: 3, username: 'chen_ming', role: 'USER', status: 1, vipExpireTime: '' },
  { id: 4, username: 'demo_disabled', role: 'USER', status: 0, vipExpireTime: '' }
]

let knowledgeList = [
  {
    id: 'DEMO_KNOWLEDGE_001',
    document: '减脂期间建议每餐包含优质蛋白、蔬菜和适量低 GI 主食，避免长期极低热量摄入。',
    metadata: {
      category: '减脂',
      title: '减脂餐盘结构',
      keywords: '减脂,蛋白质,低GI',
      status: 'enabled',
      level: 'all'
    }
  },
  {
    id: 'DEMO_KNOWLEDGE_002',
    document: '控糖人群应优先选择燕麦、杂豆、糙米等主食，并关注餐后血糖反应。',
    metadata: {
      category: '控糖',
      title: '低 GI 主食选择',
      keywords: '控糖,主食,低GI',
      status: 'enabled',
      level: 'basic'
    }
  },
  {
    id: 'DEMO_KNOWLEDGE_003',
    document: 'VIP 用户可以结合每周体重趋势复盘训练强度，避免连续高强度训练造成恢复不足。',
    metadata: {
      category: '运动',
      title: '训练恢复建议',
      keywords: '运动,恢复,VIP',
      status: 'enabled',
      level: 'vip'
    }
  }
]

let plans = [
  createPlan(301, '2026-06-08T08:30:00', '减脂', '基础版', '近 10 天体重下降 1.7kg，建议保持当前节奏，避免过度节食。'),
  createPlan(302, '2026-06-05T20:10:00', '控糖', 'VIP', '晚餐后体重波动较小，可以继续使用低 GI 主食搭配蛋白质。'),
  createPlan(303, '2026-06-01T07:45:00', '保持健康', '基础版', '体重整体平稳，适合增加轻力量训练改善体态。')
]

const profile = {
  age: 32,
  gender: '女',
  height: 166,
  weight: 71.9,
  activityLevel: '中等',
  dietPreference: '少油少糖，晚餐偏清淡',
  goal: '减脂'
}

export function createMockResponse(config) {
  const path = normalizePath(config.url)
  const method = (config.method || 'get').toLowerCase()

  return new Promise((resolve, reject) => {
    window.setTimeout(() => {
      try {
        resolve({
          data: {
            code: 200,
            message: 'DEMO_MOCK_DATA',
            data: routeMock(method, path, config)
          },
          status: 200,
          statusText: 'OK',
          headers: {},
          config
        })
      } catch (error) {
        reject({
          response: {
            status: error.status || 404,
            data: { message: error.message || 'DEMO_MOCK_DATA 未配置该接口' }
          },
          config
        })
      }
    }, MOCK_DELAY)
  })
}

function routeMock(method, path, config) {
  if (method === 'post' && path === '/user/login') {
    const body = parseBody(config.data)
    const isAdmin = body.username?.toLowerCase().includes('admin')
    const isVip = body.username?.toLowerCase().includes('vip')
    return {
      token: isAdmin ? 'frontend-admin-demo-token' : 'frontend-user-demo-token',
      userId: isAdmin ? 1 : isVip ? 2 : 3,
      username: body.username || (isAdmin ? 'admin_preview' : isVip ? 'lin_xiaoyu' : 'chen_ming'),
      role: isAdmin ? 'ADMIN' : isVip ? 'VIP' : 'USER'
    }
  }

  if (method === 'post' && path === '/user/register') {
    const body = parseBody(config.data)
    return {
      token: 'frontend-user-demo-token',
      userId: 5,
      username: body.username || 'new_demo_user',
      role: 'USER'
    }
  }

  if (method === 'get' && path === '/user/current') {
    return users[2]
  }

  if (method === 'get' && path === '/profile') {
    return profile
  }

  if ((method === 'post' || method === 'put') && path === '/profile') {
    Object.assign(profile, parseBody(config.data))
    return profile
  }

  if (method === 'get' && path === '/weight/history') {
    return weightRecords
  }

  if (method === 'post' && path === '/weight/add') {
    const body = parseBody(config.data)
    const record = {
      id: Date.now(),
      recordDate: body.recordDate,
      weight: body.weight
    }
    weightRecords = [...weightRecords, record]
    return record
  }

  const weightDeleteMatch = path.match(/^\/weight\/(\d+)$/)
  if (method === 'delete' && weightDeleteMatch) {
    weightRecords = weightRecords.filter((item) => String(item.id) !== weightDeleteMatch[1])
    return true
  }

  if (method === 'get' && path === '/plan/generate-count') {
    const isVip = getCurrentRole() === 'VIP'
    const limitCount = isVip ? 5 : 2
    const usedCount = Math.min(plans.length, limitCount)
    return { usedCount, limitCount, remainingCount: Math.max(0, limitCount - usedCount) }
  }

  if (method === 'post' && path === '/plan/generate') {
    const isVip = getCurrentRole() === 'VIP'
    const plan = createPlan(
      Date.now(),
      new Date().toISOString().slice(0, 19),
      '减脂',
      isVip ? 'VIP' : '基础版',
      '模拟生成：体重趋势持续下降，建议继续保持高蛋白早餐和每周 3 次力量训练。'
    )
    plans = [plan, ...plans]
    return plan
  }

  if (method === 'get' && path === '/plan/history') {
    return plans
  }

  const planDetailMatch = path.match(/^\/plan\/(.+)$/)
  if (method === 'get' && planDetailMatch) {
    return plans.find((item) => String(item.id) === planDetailMatch[1]) || notFound('未找到计划')
  }

  if (method === 'post' && path === '/chat/ask') {
    const body = parseBody(config.data)
    return {
      answer: `已收到问题：“${body.question || '健康问题'}”。建议先保证规律饮食、足量饮水和稳定运动频率，如有疾病诊断请以医生建议为准。`,
      references: knowledgeList.slice(0, 2).map((item) => ({
        category: item.metadata.category,
        title: item.metadata.title
      }))
    }
  }

  if (path === '/admin/users' && method === 'get') {
    const params = config.params || {}
    const keyword = params.keyword?.toLowerCase()
    const filtered = keyword ? users.filter((item) => item.username.toLowerCase().includes(keyword)) : users
    const page = Number(params.page || 1)
    const size = Number(params.size || 10)
    return {
      records: filtered.slice((page - 1) * size, page * size),
      total: filtered.length
    }
  }

  const adminUserMatch = path.match(/^\/admin\/users\/(\d+)$/)
  if (adminUserMatch && method === 'get') {
    return users.find((item) => String(item.id) === adminUserMatch[1]) || notFound('未找到用户')
  }

  const adminStatusMatch = path.match(/^\/admin\/users\/(\d+)\/(enable|disable)$/)
  if (adminStatusMatch && method === 'put') {
    updateUserStatus(adminStatusMatch[1], adminStatusMatch[2] === 'enable' ? 1 : 0)
    return true
  }

  const adminWeightsMatch = path.match(/^\/admin\/users\/(\d+)\/weights$/)
  if (adminWeightsMatch && method === 'get') {
    return weightRecords
  }

  const adminPlansMatch = path.match(/^\/admin\/users\/(\d+)\/plans$/)
  if (adminPlansMatch && method === 'get') {
    return plans
  }

  if (path === '/admin/knowledge/list' && method === 'get') {
    const params = config.params || {}
    return knowledgeList.filter((item) => {
      const keywordMatch = !params.keyword || item.metadata.title.includes(params.keyword) || item.document.includes(params.keyword)
      const categoryMatch = !params.category || item.metadata.category === params.category
      const statusMatch = !params.status || item.metadata.status === params.status
      const levelMatch = !params.level || item.metadata.level === params.level
      return keywordMatch && categoryMatch && statusMatch && levelMatch
    })
  }

  if (path === '/admin/knowledge' && method === 'post') {
    const item = {
      ...parseBody(config.data),
      id: `DEMO_KNOWLEDGE_${Date.now()}`
    }
    knowledgeList = [item, ...knowledgeList]
    return item
  }

  const knowledgeMatch = path.match(/^\/admin\/knowledge\/([^/]+)$/)
  if (knowledgeMatch && method === 'put') {
    knowledgeList = knowledgeList.map((item) => (item.id === knowledgeMatch[1] ? { ...item, ...parseBody(config.data) } : item))
    return true
  }

  if (knowledgeMatch && method === 'delete') {
    knowledgeList = knowledgeList.filter((item) => item.id !== knowledgeMatch[1])
    return true
  }

  const knowledgeStatusMatch = path.match(/^\/admin\/knowledge\/([^/]+)\/(enable|disable)$/)
  if (knowledgeStatusMatch && method === 'put') {
    knowledgeList = knowledgeList.map((item) =>
      item.id === knowledgeStatusMatch[1]
        ? { ...item, metadata: { ...item.metadata, status: knowledgeStatusMatch[2] === 'enable' ? 'enabled' : 'disabled' } }
        : item
    )
    return true
  }

  return notFound()
}

function createPlan(id, createTime, planType, planLevel, trendSummary) {
  const isVipPlan = planLevel === 'VIP'
  return {
    id,
    createTime,
    planType,
    planLevel,
    trendSummary,
    planContent: {
      dietPlan: [
        {
          day: '周一',
          breakfast: '燕麦牛奶 + 水煮蛋',
          lunch: '鸡胸肉糙米饭 + 西兰花',
          dinner: '番茄豆腐汤 + 小份红薯',
          snack: '无糖酸奶',
          calorie: createCalorie('约 1680 kcal', [
            ['燕麦牛奶', '360 kcal'],
            ['水煮蛋', '75 kcal'],
            ['鸡胸肉糙米饭', '620 kcal'],
            ['西兰花', '70 kcal'],
            ['番茄豆腐汤', '220 kcal'],
            ['小份红薯', '180 kcal'],
            ['无糖酸奶', '155 kcal']
          ], '热量落在目标区间，晚餐主食量适中，适合继续保持。')
        },
        {
          day: '周二',
          breakfast: '全麦吐司 + 牛油果',
          lunch: '清蒸鱼 + 杂粮饭',
          dinner: '虾仁蔬菜沙拉',
          snack: '一小把坚果',
          calorie: createCalorie('约 1720 kcal', [
            ['全麦吐司', '210 kcal'],
            ['牛油果', '160 kcal'],
            ['清蒸鱼', '260 kcal'],
            ['杂粮饭', '280 kcal'],
            ['虾仁蔬菜沙拉', '360 kcal'],
            ['一小把坚果', '150 kcal'],
            ['调味与烹调用油', '300 kcal']
          ], '脂肪来源较健康，但坚果控制在 10-15g，避免热量悄悄超标。')
        },
        {
          day: '周三',
          breakfast: '豆浆 + 鸡蛋 + 玉米',
          lunch: '牛肉蔬菜碗',
          dinner: '菌菇青菜 + 豆腐',
          snack: '苹果半个',
          calorie: createCalorie('约 1600 kcal', [
            ['豆浆', '120 kcal'],
            ['鸡蛋', '75 kcal'],
            ['玉米', '170 kcal'],
            ['牛肉蔬菜碗', '640 kcal'],
            ['菌菇青菜', '110 kcal'],
            ['豆腐', '180 kcal'],
            ['苹果半个', '55 kcal'],
            ['调味与烹调用油', '250 kcal']
          ], '略低于目标，力量训练日可在午餐增加半拳主食，提高训练恢复。')
        },
        {
          day: '周四',
          breakfast: '希腊酸奶 + 蓝莓 + 燕麦',
          lunch: '番茄牛腩 + 半碗糙米饭',
          dinner: '鸡蛋蔬菜汤 + 玉米半根',
          snack: '黄瓜条 + 低脂奶酪',
          calorie: createCalorie('约 1660 kcal', [
            ['希腊酸奶', '140 kcal'],
            ['蓝莓', '45 kcal'],
            ['燕麦', '210 kcal'],
            ['番茄牛腩', '560 kcal'],
            ['半碗糙米饭', '150 kcal'],
            ['鸡蛋蔬菜汤', '220 kcal'],
            ['玉米半根', '90 kcal'],
            ['低脂奶酪', '95 kcal'],
            ['调味与烹调用油', '150 kcal']
          ], '蛋白质和碳水分配均衡，晚餐清淡但不空腹，适合作为稳定减脂日。')
        },
        {
          day: '周五',
          breakfast: '杂粮粥 + 茶叶蛋',
          lunch: '虾仁荞麦面 + 生菜',
          dinner: '清蒸鱼 + 烫青菜',
          snack: '无糖豆浆',
          calorie: createCalorie('约 1580 kcal', [
            ['杂粮粥', '240 kcal'],
            ['茶叶蛋', '80 kcal'],
            ['虾仁荞麦面', '520 kcal'],
            ['生菜', '30 kcal'],
            ['清蒸鱼', '260 kcal'],
            ['烫青菜', '70 kcal'],
            ['无糖豆浆', '110 kcal'],
            ['调味与烹调用油', '270 kcal']
          ], '总热量偏低，晚餐可加小份南瓜或红薯，避免周末前饥饿感反弹。')
        },
        {
          day: '周六',
          breakfast: '全麦贝果半个 + 鸡胸肉片',
          lunch: '鸡腿去皮 + 红薯 + 彩椒',
          dinner: '冬瓜海带汤 + 豆腐',
          snack: '奇亚籽酸奶',
          calorie: createCalorie('约 1740 kcal', [
            ['全麦贝果半个', '180 kcal'],
            ['鸡胸肉片', '160 kcal'],
            ['鸡腿去皮', '360 kcal'],
            ['红薯', '220 kcal'],
            ['彩椒', '45 kcal'],
            ['冬瓜海带汤', '120 kcal'],
            ['豆腐', '190 kcal'],
            ['奇亚籽酸奶', '210 kcal'],
            ['调味与烹调用油', '255 kcal']
          ], '接近目标上限，骑行或慢跑日可以接受，注意晚餐少油。')
        },
        {
          day: '周日',
          breakfast: '鸡蛋蔬菜卷 + 黑咖啡',
          lunch: '牛肉藜麦沙拉',
          dinner: '菌菇鸡丝汤 + 小份南瓜',
          snack: '猕猴桃 1 个',
          calorie: createCalorie('约 1620 kcal', [
            ['鸡蛋蔬菜卷', '320 kcal'],
            ['黑咖啡', '5 kcal'],
            ['牛肉藜麦沙拉', '610 kcal'],
            ['菌菇鸡丝汤', '250 kcal'],
            ['小份南瓜', '120 kcal'],
            ['猕猴桃', '60 kcal'],
            ['调味与烹调用油', '255 kcal']
          ], '恢复日热量略低但结构清爽，可根据饥饿感加一杯无糖酸奶。')
        }
      ],
      exercisePlan: [
        createExercise('周一', '快走 + 拉伸', '40 分钟', '中低强度', [
          ['热身', '原地踏步 5 分钟，肩颈环绕各 10 次'],
          ['主项目', '快走 25 分钟，保持能说话但略喘的速度'],
          ['收尾', '小腿后侧、股四头肌、臀部各拉伸 30 秒']
        ], '适合减脂起始日，不追求速度，重点是稳定完成。'),
        createExercise('周二', '上肢力量 + 肩颈放松', '30 分钟', '中等强度', [
          ['推墙俯卧撑', '3 组，每组 10-12 次'],
          ['弹力带划船', '3 组，每组 12 次'],
          ['肩颈放松', '靠墙天使 2 组，每组 10 次']
        ], '动作节奏放慢，肩膀不要耸起，训练后做 3 分钟胸肩拉伸。'),
        createExercise('周三', '力量训练', '35 分钟', '中等强度', [
          ['深蹲', '3 组，每组 12 次'],
          ['臀桥', '3 组，每组 15 次'],
          ['平板支撑', '3 组，每组 30 秒']
        ], '每组间休息 60-90 秒，保持膝盖和脚尖方向一致。'),
        createExercise('周四', '核心稳定训练', '25 分钟', '中等强度', [
          ['死虫式', '3 组，每侧 10 次'],
          ['鸟狗式', '3 组，每侧 10 次'],
          ['侧桥', '2 组，每侧 20 秒']
        ], '核心训练不憋气，腰部保持稳定，动作质量优先。'),
        createExercise('周五', '低冲击有氧 + 拉伸', '35 分钟', '中低强度', [
          ['椭圆机或踏步', '20 分钟，心率保持舒适区间'],
          ['动态拉伸', '髋部绕环、腿后侧摆动各 10 次'],
          ['静态拉伸', '大腿前侧、背部、髋屈肌各 30 秒']
        ], '作为工作周收尾，降低冲击，避免疲劳积累。'),
        createExercise('周六', '骑行或慢跑', '45 分钟', '中等强度', [
          ['热身', '慢走或轻松骑行 8 分钟'],
          ['主项目', '骑行或慢跑 30 分钟，保持稳定节奏'],
          ['冷身', '慢走 5 分钟后拉伸小腿和臀部']
        ], '如果膝盖不适，优先选择骑行或快走替代慢跑。'),
        createExercise('周日', '瑜伽 + 充分拉伸', '30 分钟', '低强度恢复', [
          ['猫牛式', '2 组，每组 8-10 次'],
          ['婴儿式', '保持 60 秒'],
          ['鸽子式', '每侧保持 45 秒']
        ], '恢复日以放松和睡眠质量为目标，不需要额外增加强度。')
      ],
      vipDetail: isVipPlan
        ? {
            calorieEstimate: '每日 1650-1750 kcal',
            nutritionRatio: '蛋白质 30%，碳水 40%，脂肪 30%',
            mealStrategy: '每餐优先保证一掌心蛋白质、两拳蔬菜和半拳低 GI 主食，晚餐主食不完全取消。',
            trainingFocus: '本周以 2 次力量训练维持肌肉量，搭配 2 次有氧提高消耗，避免连续两天高强度。',
            recoveryPlan: '训练日睡眠尽量达到 7 小时以上，力量训练后 30-60 分钟补充蛋白质和少量碳水。',
            hydrationTarget: '每日饮水 1800-2200 ml，运动日额外补充 300-500 ml。',
            riskReminder: '近 10 天下降较快时不要继续压低热量；若出现乏力、头晕或经期异常，应暂停减脂强度。',
            weeklyReview: '每周固定同一时间称重 3 次，观察平均值而不是单日波动。'
          }
        : undefined
    },
    references: knowledgeList.slice(0, 2).map((item) => ({
      category: item.metadata.category,
      title: item.metadata.title
    }))
  }
}

function createCalorie(total, foods, advice) {
  return {
    total,
    target: '1650-1750 kcal',
    foods: foods.map(([name, kcal]) => ({ name, kcal })),
    advice
  }
}

function createExercise(day, type, duration, intensity, items, note) {
  return {
    day,
    type,
    duration,
    intensity,
    items: items.map(([name, detail]) => ({ name, detail })),
    note
  }
}

function getCurrentRole() {
  try {
    return JSON.parse(window.localStorage.getItem('smart_health_user') || '{}').role || 'USER'
  } catch {
    return 'USER'
  }
}

function updateUserStatus(id, status) {
  users = users.map((item) => (String(item.id) === String(id) ? { ...item, status } : item))
}

function normalizePath(url = '') {
  const absoluteUrl = new URL(url, window.location.origin)
  return absoluteUrl.pathname.replace(/^\/api\/v1/, '')
}

function parseBody(data) {
  if (!data) {
    return {}
  }
  return typeof data === 'string' ? JSON.parse(data) : data
}

function notFound(message = 'DEMO_MOCK_DATA 未配置该接口') {
  const error = new Error(message)
  error.status = 404
  throw error
}
