# 后端对接说明：AI 计划与首页数据同步

## 1. 背景

前端当前使用 mock 数据完成了以下功能展示：

- 首页同步展示当前用户身份、健康档案、体重记录、AI 计划次数和最新历史计划。
- AI 计划页展示 7 天饮食计划和 7 天运动计划。
- VIP 用户可看到每日饮食热量估算、目标区间和当天建议。
- VIP 用户可看到更丰富的详细建议。

后续删除 mock 数据并正式接入后端时，后端需要保证相关接口和响应字段结构一致，否则前端页面仍可运行，但部分内容会缺失。

## 2. 首页依赖接口

首页会同时读取以下接口：

| 接口 | 用途 |
| --- | --- |
| `GET /user/current` | 获取当前用户身份，判断普通用户或 VIP 用户 |
| `GET /profile` | 判断健康档案是否已同步，并展示年龄、身高、目标 |
| `GET /weight/history` | 展示最新体重、近几次体重变化和首页趋势图 |
| `GET /plan/generate-count` | 展示今日 AI 计划剩余次数和已用次数 |
| `GET /plan/history` | 展示最新 AI 计划摘要 |

注意：

- `GET /user/current` 返回的 `role` 必须准确，前端会根据 `USER` / `VIP` 显示不同功能入口。
- `GET /weight/history` 建议按日期升序或可排序字段返回，前端会按 `recordDate` 排序。
- `GET /plan/history` 建议包含最新计划的 `planLevel`、`planType`、`trendSummary`、`createTime`。

## 3. AI 计划生成接口要求

接口：

```text
POST /plan/generate
```

前端期望返回结构：

```json
{
  "code": 200,
  "message": "生成成功",
  "data": {
    "id": 1,
    "planLevel": "VIP",
    "planType": "减脂",
    "trendSummary": "体重趋势持续下降，建议继续保持高蛋白早餐和每周 3 次力量训练。",
    "createTime": "2026-06-08T11:19:50",
    "planContent": {
      "dietPlan": [],
      "exercisePlan": [],
      "vipDetail": {}
    },
    "references": []
  }
}
```

`id` 或 `planId` 建议统一。当前前端历史详情和 mock 使用 `id`，如果后端使用 `planId`，建议前端和后端再统一一次字段名。

## 4. 饮食计划字段

`planContent.dietPlan` 需要返回 7 天数据，建议周一到周日完整返回。

每一天结构：

```json
{
  "day": "周一",
  "breakfast": "燕麦牛奶 + 水煮蛋",
  "lunch": "鸡胸肉糙米饭 + 西兰花",
  "dinner": "番茄豆腐汤 + 小份红薯",
  "snack": "无糖酸奶",
  "calorie": {
    "total": "约 1680 kcal",
    "target": "1650-1750 kcal",
    "advice": "热量落在目标区间，晚餐主食量适中，适合继续保持。"
  }
}
```

字段说明：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `day` | string | 是 | 周一到周日 |
| `breakfast` | string | 是 | 早餐建议 |
| `lunch` | string | 是 | 午餐建议 |
| `dinner` | string | 是 | 晚餐建议 |
| `snack` | string | 是 | 加餐建议 |
| `calorie.total` | string | VIP 建议必填 | 当天预计摄入热量 |
| `calorie.target` | string | VIP 建议必填 | 当天目标热量区间 |
| `calorie.advice` | string | VIP 建议必填 | 针对当天餐单的建议 |

普通用户可以不返回 `calorie`，前端会自动隐藏热量模块。VIP 用户建议返回。

## 5. 运动计划字段

`planContent.exercisePlan` 也需要返回 7 天数据，与饮食计划按 `day` 对齐。

每一天结构：

```json
{
  "day": "周一",
  "type": "快走 + 拉伸",
  "duration": "40 分钟",
  "intensity": "中低强度"
}
```

字段说明：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `day` | string | 是 | 周一到周日 |
| `type` | string | 是 | 训练类型 |
| `duration` | string | 是 | 训练时长 |
| `intensity` | string | 是 | 训练强度 |

注意：

- 饮食计划和运动计划都建议返回 7 天。
- 前端会按 `day` 将饮食和运动组合成同一天的一行卡片。
- 如果某一天运动缺失，页面不会报错，但会显示不完整。

## 6. VIP 详细建议字段

`planContent.vipDetail` 建议返回以下字段：

```json
{
  "calorieEstimate": "每日 1650-1750 kcal",
  "nutritionRatio": "蛋白质 30%，碳水 40%，脂肪 30%",
  "mealStrategy": "每餐优先保证一掌心蛋白质、两拳蔬菜和半拳低 GI 主食，晚餐主食不完全取消。",
  "trainingFocus": "本周以 2 次力量训练维持肌肉量，搭配 2 次有氧提高消耗，避免连续两天高强度。",
  "recoveryPlan": "训练日睡眠尽量达到 7 小时以上，力量训练后 30-60 分钟补充蛋白质和少量碳水。",
  "hydrationTarget": "每日饮水 1800-2200 ml，运动日额外补充 300-500 ml。",
  "riskReminder": "近 10 天下降较快时不要继续压低热量；若出现乏力、头晕或经期异常，应暂停减脂强度。",
  "weeklyReview": "每周固定同一时间称重 3 次，观察平均值而不是单日波动。"
}
```

字段说明：

| 字段 | 说明 |
| --- | --- |
| `calorieEstimate` | 总体热量目标 |
| `nutritionRatio` | 宏量营养比例 |
| `mealStrategy` | 餐盘结构和饮食执行建议 |
| `trainingFocus` | 本周训练重点 |
| `recoveryPlan` | 训练恢复建议 |
| `hydrationTarget` | 饮水目标 |
| `riskReminder` | 风险提醒 |
| `weeklyReview` | 每周复盘建议 |

前端会自动渲染存在的字段；如果某个字段缺失，会自动跳过。

## 7. 历史计划接口要求

接口：

```text
GET /plan/history
GET /plan/{id}
```

`GET /plan/history` 至少需要返回：

```json
[
  {
    "id": 1,
    "planType": "减脂",
    "planLevel": "VIP",
    "trendSummary": "体重趋势持续下降，建议继续保持高蛋白早餐和每周 3 次力量训练。",
    "createTime": "2026-06-08T11:19:50"
  }
]
```

`GET /plan/{id}` 需要返回完整 `planContent`，结构与 `POST /plan/generate` 一致，方便历史详情展示。

## 8. AI 生成提示词建议

后端调用 DeepSeek 时，建议明确要求模型返回固定 JSON，不要返回 Markdown。

建议 Prompt 约束：

```text
请只返回 JSON，不要返回 Markdown。
dietPlan 必须包含周一到周日 7 天。
exercisePlan 必须包含周一到周日 7 天。
VIP 用户每一天 dietPlan 必须包含 calorie.total、calorie.target、calorie.advice。
vipDetail 必须尽量包含 calorieEstimate、nutritionRatio、mealStrategy、trainingFocus、recoveryPlan、hydrationTarget、riskReminder、weeklyReview。
所有健康建议仅作为健康管理参考，不能替代医疗建议。
```

## 9. 普通用户与 VIP 用户差异

普通用户：

- 每日 AI 计划生成次数限制为 2 次。
- 可以返回基础 7 天饮食和运动计划。
- 可以不返回 `dietPlan[].calorie`。
- 可以不返回 `vipDetail`。
- 不允许调用 `POST /chat/ask`。

VIP 用户：

- 每日 AI 计划生成次数限制为 5 次。
- 返回 7 天饮食计划。
- 返回 7 天运动计划。
- 返回每日饮食热量估算和建议。
- 返回更完整的 `vipDetail`。
- 允许调用 `POST /chat/ask`。

## 10. 删除 mock 后的验收点

后端接入完成后，请至少验证：

- 首页可以显示真实当前用户身份，VIP 用户显示为 VIP。
- 首页健康档案显示为已同步。
- 首页体重记录显示最新体重和变化趋势。
- 首页 AI 计划次数来自 `GET /plan/generate-count`。
- 首页最新计划摘要来自 `GET /plan/history`。
- AI 计划页饮食和运动均为 7 天。
- 饮食和运动按同一天对齐展示。
- VIP 用户每天饮食卡片底部显示热量估算、目标区间和建议。
- 普通用户不显示 VIP 详细建议和智能问答入口。
- VIP 用户可以进入智能问答页。

## 11. 数据库存储建议

如果当前计划内容存储在 `plan.plan_content` 文本字段中，可以继续以 JSON 字符串保存上述结构。

建议保存前校验 JSON Schema，避免 AI 返回字段缺失导致前端展示不完整。

如后续需要统计每日热量，可考虑将每日计划拆表。但当前版本不强制拆表，直接存 JSON 即可满足前端展示。
