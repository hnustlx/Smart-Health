/**
 * 回车到下一栏 (Enter-to-next-field)
 *
 * 用法:
 *   const { setRef, onEnter } = useEnterToNext(2)
 *   // template:
 *   <el-input :ref="setRef(0)" @keyup.enter="onEnter(0)" />
 *   <el-input :ref="setRef(1)" @keyup.enter="onEnter(1)" />  ← 最后一个自动提交表单
 */
import { ref } from 'vue'

export function useEnterToNext(total) {
  const refs = ref(Array(total))

  function setRef(i) {
    return (el) => { refs.value[i] = el }
  }

  function onEnter(i) {
    const next = i + 1
    if (next < total && refs.value[next]) {
      refs.value[next].focus()
    }
    // 最后一个字段：不阻止默认行为，让 el-form @submit.prevent 处理提交
  }

  return { setRef, onEnter }
}
