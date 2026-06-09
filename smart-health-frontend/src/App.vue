<template>
  <router-view />
  <button
    class="theme-toggle"
    :class="{ 'theme-toggle--night': theme === 'night' }"
    type="button"
    :aria-label="themeLabel"
    :aria-pressed="theme === 'night'"
    @click="toggleTheme"
  >
    <span class="theme-toggle__thumb" aria-hidden="true">
      <span class="theme-toggle__icon theme-toggle__icon--sun"></span>
      <span class="theme-toggle__icon theme-toggle__icon--moon"></span>
    </span>
  </button>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'

const THEME_KEY = 'smart_health_theme'
const theme = ref('day')
const themeLabel = computed(() => (theme.value === 'day' ? '切换夜间模式' : '切换白日模式'))

onMounted(() => {
  const savedTheme = localStorage.getItem(THEME_KEY)
  theme.value = savedTheme === 'dark' ? 'night' : savedTheme || 'day'
  applyTheme()
})

function toggleTheme() {
  theme.value = theme.value === 'day' ? 'night' : 'day'
  localStorage.setItem(THEME_KEY, theme.value)
  applyTheme()
}

function applyTheme() {
  document.documentElement.dataset.theme = theme.value
}
</script>
