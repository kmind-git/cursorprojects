<template>
  <div class="min-h-dvh fintech-bg text-white">
    <div class="mx-auto flex min-h-dvh w-full max-w-[1220px] flex-col px-4 pb-4 pt-4 sm:px-6 sm:pb-5 sm:pt-5 lg:px-10">
      <div
        class="flex min-h-0 flex-1 flex-col items-start gap-8 lg:grid lg:min-h-0 lg:content-start lg:grid-cols-[minmax(0,1fr)_minmax(min(100%,280px),min(360px,29vw))] lg:items-start lg:gap-x-16 lg:gap-y-0 xl:grid-cols-[minmax(0,1fr)_minmax(min(100%,300px),min(400px,31vw))]"
      >
        <section
          class="relative z-10 hidden min-h-0 w-full flex-col lg:-translate-x-24 lg:flex lg:pr-6"
        >
          <div class="shrink-0 space-y-14">
            <div class="flex items-center gap-3">
              <div class="grid h-10 w-10 place-items-center rounded-xl border border-blue-500/30 bg-blue-500/15">
                <svg class="h-6 w-6 text-blue-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                  <path d="M12 3l8 4.8v8.4L12 21l-8-4.8V7.8L12 3z" />
                  <path d="M8.5 14.5V9.8l3.5-2.1 3.5 2.1v4.7l-3.5 2.1-3.5-2.1z" />
                </svg>
              </div>
              <div>
                <div class="text-[38px] font-bold tracking-[-0.03em] leading-none">
                  Global<span class="text-blue-500">Trade</span>
                </div>
                <div class="mt-1 text-lg text-slate-300/85">全球交易投资系统</div>
              </div>
            </div>

            <div>
              <h1 class="text-[64px] font-extrabold leading-[1.08] tracking-[-0.04em]">
                全球视野<br />
                智慧<span class="text-blue-500">投资</span>
              </h1>
              <p class="mt-6 text-[34px] text-slate-300/90">连接全球市场，把握投资机遇</p>
            </div>

            <div class="relative z-20 grid grid-cols-3 gap-7">
              <article v-for="item in features" :key="item.title" class="space-y-2.5">
                <div
                  class="grid h-[70px] w-[70px] place-items-center rounded-[18px] border border-[#1a3d83]/45 bg-[#081739]/95 shadow-[inset_0_1px_0_rgba(255,255,255,0.03),0_8px_20px_rgba(4,12,34,0.35)]"
                >
                  <svg
                    v-if="item.icon === 'market'"
                    class="h-10 w-10 shrink-0 text-[#2d6bff]"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.8"
                  >
                    <circle cx="12" cy="12" r="8.5" />
                    <path d="M3.5 12h17M12 3.5a13.6 13.6 0 0 1 0 17M12 3.5a13.6 13.6 0 0 0 0 17" />
                  </svg>
                  <svg
                    v-else-if="item.icon === 'secure'"
                    class="h-10 w-10 shrink-0 text-[#2d6bff]"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.8"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <path d="M12 3l7 3.8v5.7c0 4.5-3 8.2-7 9.5-4-1.3-7-5-7-9.5V6.8L12 3z" />
                    <path d="M9.5 12.2l1.7 1.7 3.3-3.4" />
                  </svg>
                  <svg
                    v-else
                    class="h-10 w-10 shrink-0 text-[#2d6bff]"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <path d="M13.5 2L5 13h5.3L9.8 22 19 10.5h-5.1L13.5 2z" />
                  </svg>
                </div>
                <h3 class="text-[28px] font-bold leading-[1.16] tracking-[-0.02em] text-slate-50">{{ item.title }}</h3>
                <p class="max-w-[245px] text-[15px] leading-[1.58] text-slate-500/62">{{ item.desc }}</p>
              </article>
            </div>
          </div>

          <div class="mt-10 flex max-w-[760px] flex-col gap-3 lg:mt-14">
            <div
              class="flex items-center gap-2 overflow-hidden rounded-xl border border-blue-400/20 bg-[#071633]/70 px-4 py-2.5 backdrop-blur-sm sm:gap-3 sm:px-5"
            >
              <div
                v-for="item in marketTicker"
                :key="item.name"
                class="flex min-w-0 flex-1 items-center justify-between gap-2 rounded-lg bg-[#091c42]/80 px-3 py-1.5"
              >
                <span class="whitespace-nowrap text-[12px] font-medium text-slate-300/85 sm:text-[13px]">{{ item.name }}</span>
                <span
                  :class="item.trend === 'up' ? 'text-emerald-300' : 'text-rose-300'"
                  class="shrink-0 whitespace-nowrap text-[12px] font-semibold sm:text-[13px]"
                >
                  {{ item.value }}
                </span>
              </div>
            </div>
            <div
              class="flex items-center gap-2 overflow-hidden rounded-xl border border-blue-400/20 bg-[#071633]/70 px-4 py-2.5 backdrop-blur-sm sm:gap-3 sm:px-5"
            >
              <div
                v-for="item in chinaTicker"
                :key="item.name"
                class="flex min-w-0 flex-1 items-center justify-between gap-2 rounded-lg bg-[#091c42]/80 px-3 py-1.5"
              >
                <span class="whitespace-nowrap text-[12px] font-medium text-slate-300/85 sm:text-[13px]">{{ item.name }}</span>
                <span
                  :class="item.trend === 'up' ? 'text-emerald-300' : 'text-rose-300'"
                  class="shrink-0 whitespace-nowrap text-[12px] font-semibold sm:text-[13px]"
                >
                  {{ item.value }}
                </span>
              </div>
            </div>
          </div>
        </section>

        <section
          class="flex min-h-0 w-full items-start justify-center lg:w-full lg:self-stretch lg:translate-x-8 lg:justify-end xl:translate-x-12"
        >
          <div
            class="login-card-scroll w-full max-w-full rounded-2xl border border-blue-300/15 bg-[#081633]/70 p-6 shadow-[0_16px_48px_rgba(0,0,0,0.4)] backdrop-blur-[8px] sm:max-w-[min(20rem,94vw)] sm:p-7 lg:flex lg:h-full lg:flex-col lg:max-w-none lg:p-8"
          >
            <div class="mb-5 text-center sm:mb-6">
              <div class="mx-auto mb-3 grid h-11 w-11 place-items-center rounded-full border border-blue-400/30 bg-blue-500/15 sm:h-12 sm:w-12">
                <svg class="h-6 w-6 text-blue-400 sm:h-7 sm:w-7" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                  <circle cx="12" cy="12" r="9" />
                  <path d="M3 12h18M12 3a15 15 0 0 1 0 18M12 3a15 15 0 0 0 0 18" />
                </svg>
              </div>
              <h2 class="text-2xl font-extrabold tracking-tight sm:text-3xl">欢迎登录</h2>
              <p class="mt-1.5 text-sm text-slate-300/80 sm:text-base">全球交易投资系统</p>
            </div>

            <form @submit.prevent="handleLogin" class="space-y-4 sm:space-y-5">
              <div class="space-y-1.5">
                <label class="text-sm font-medium text-slate-200/95 sm:text-base">用户名</label>
                <div class="relative">
                  <span class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">
                    <svg class="h-4 w-4 sm:h-5 sm:w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                      <circle cx="12" cy="8" r="4" />
                      <path d="M4 20a8 8 0 0 1 16 0" />
                    </svg>
                  </span>
                  <input
                    v-model="form.username"
                    type="text"
                    placeholder="请输入用户名"
                    autocomplete="username"
                    class="h-11 w-full rounded-lg border border-blue-300/20 bg-[#061229]/80 pl-10 pr-3 text-sm text-slate-100 placeholder:text-slate-400/80 focus:border-blue-400/55 focus:outline-none sm:h-12 sm:rounded-xl sm:pl-11 sm:pr-4 sm:text-base"
                  />
                </div>
              </div>

              <div class="space-y-1.5">
                <label class="text-sm font-medium text-slate-200/95 sm:text-base">密码</label>
                <div class="relative">
                  <span class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">
                    <svg class="h-4 w-4 sm:h-5 sm:w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                      <rect x="5" y="11" width="14" height="10" rx="2" />
                      <path d="M8 11V8a4 4 0 1 1 8 0v3" />
                    </svg>
                  </span>
                  <input
                    v-model="form.password"
                    :type="showPassword ? 'text' : 'password'"
                    placeholder="请输入密码"
                    autocomplete="current-password"
                    class="h-11 w-full rounded-lg border border-blue-300/20 bg-[#061229]/80 pl-10 pr-10 text-sm text-slate-100 placeholder:text-slate-400/80 focus:border-blue-400/55 focus:outline-none sm:h-12 sm:rounded-xl sm:pl-11 sm:pr-12 sm:text-base"
                  />
                  <button
                    type="button"
                    @click="showPassword = !showPassword"
                    class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-200 sm:right-4"
                  >
                    <svg class="h-4 w-4 sm:h-5 sm:w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                      <path d="M2 12s3.8-6 10-6 10 6 10 6-3.8 6-10 6-10-6-10-6z" />
                      <circle cx="12" cy="12" r="3" />
                    </svg>
                  </button>
                </div>
              </div>

              <div class="flex items-center justify-between text-xs text-slate-300/85 sm:text-sm">
                <label class="flex cursor-pointer items-center gap-2">
                  <input v-model="form.remember" type="checkbox" class="h-3.5 w-3.5 rounded border-blue-300/30 bg-transparent sm:h-4 sm:w-4" />
                  <span>记住我</span>
                </label>
                <a href="#" class="text-blue-400 hover:text-blue-300">忘记密码？</a>
              </div>

              <p v-if="loginMessage" class="rounded-lg border border-emerald-500/30 bg-emerald-500/10 px-3 py-2 text-sm text-emerald-100 sm:px-4 sm:text-base">
                {{ loginMessage }}
              </p>
              <p v-if="err" class="rounded-lg border border-rose-500/35 bg-rose-500/10 px-3 py-2 text-sm text-rose-100 sm:px-4 sm:text-base">
                {{ err }}
              </p>

              <button
                type="submit"
                :disabled="loading"
                class="h-11 w-full rounded-lg bg-gradient-to-r from-blue-500 to-[#2f6cff] text-base font-semibold tracking-wide hover:brightness-110 disabled:cursor-not-allowed disabled:opacity-60 sm:h-12 sm:rounded-xl sm:text-lg"
              >
                {{ loading ? '登录中…' : '登录' }}
              </button>
            </form>

            <div class="my-4 flex items-center gap-3 sm:my-5">
              <div class="h-px flex-1 bg-blue-300/20" />
              <span class="shrink-0 text-xs text-slate-300/70 sm:text-sm">或使用以下方式登录</span>
              <div class="h-px flex-1 bg-blue-300/20" />
            </div>

            <div class="grid grid-cols-2 gap-2 sm:gap-3">
              <button
                type="button"
                class="flex h-9 items-center justify-center gap-1.5 rounded-lg border border-blue-300/20 bg-[#08142d]/85 text-xs text-slate-100 hover:bg-[#0b1d3d] sm:h-10 sm:gap-2 sm:text-sm"
              >
                <span class="text-base sm:text-lg">G</span>
                <span>Google</span>
              </button>
              <button
                type="button"
                class="flex h-9 items-center justify-center gap-1.5 rounded-lg border border-blue-300/20 bg-[#08142d]/85 text-xs text-slate-100 hover:bg-[#0b1d3d] sm:h-10 sm:gap-2 sm:text-sm"
              >
                <span class="text-base sm:text-lg">⊞</span>
                <span>Microsoft</span>
              </button>
            </div>

            <p class="mt-4 text-center text-xs text-slate-300/85 sm:mt-5 sm:text-sm">
              还没有账号？
              <a href="#" class="text-blue-400 hover:text-blue-300">立即注册</a>
            </p>
          </div>
        </section>
      </div>

      <footer class="mt-4 text-center text-[18px] text-slate-400/80">
        © 2024 GlobalTrade 全球交易投资系统. 保留所有权利
        <span class="mx-4">|</span>
        隐私政策
        <span class="mx-4">|</span>
        服务条款
        <span class="mx-4">|</span>
        帮助中心
      </footer>
    </div>
  </div>
</template>

<script setup>
import axios from 'axios'
import { reactive, ref } from 'vue'

const form = reactive({
  username: '',
  password: '',
  remember: false,
})

const showPassword = ref(false)
const loading = ref(false)
const err = ref('')
const loginMessage = ref('')

const features = [
  { icon: 'market', title: '全球市场', desc: '覆盖全球股票、期货、外汇、数字货币等' },
  { icon: 'secure', title: '安全可靠', desc: '多重安全防护，保障资金与数据安全' },
  { icon: 'speed', title: '极速交易', desc: '毫秒级交易执行，不错过任何机会' },
]

const marketTicker = [
  { name: 'S&P 500', value: '+0.63%', trend: 'up' },
  { name: 'NASDAQ', value: '+0.84%', trend: 'up' },
  { name: 'DOW', value: '+0.56%', trend: 'up' },
  { name: 'BTC', value: '-0.18%', trend: 'down' },
  { name: 'USD/CNY', value: '+0.09%', trend: 'up' },
]

const chinaTicker = [
  { name: '上证指数', value: '+0.72%', trend: 'up' },
  { name: '深证成指', value: '+0.65%', trend: 'up' },
  { name: '创业板指', value: '+0.43%', trend: 'up' },
  { name: '科创50', value: '-0.12%', trend: 'down' },
  { name: '北交所', value: '+0.31%', trend: 'up' },
]

function loginErrorMessage(e) {
  if (axios.isAxiosError(e)) {
    if (e.code === 'ERR_NETWORK' || !e.response) {
      return '无法连接服务器，请确认后端已启动（默认 8080）且 Vite 已代理 /api。'
    }
    const status = e.response.status
    const code = e.response.data?.code
    if (status === 400 && code === 'invalid_credentials') {
      return '用户名或密码错误'
    }
    if (status === 400 && code) {
      return `登录失败：${code}`
    }
    return `登录失败（HTTP ${status}）`
  }
  return '登录失败'
}

async function handleLogin() {
  err.value = ''
  loginMessage.value = ''
  loading.value = true
  try {
    const { data } = await axios.post(
      '/api/auth/login',
      { username: form.username, password: form.password },
      { withCredentials: true },
    )
    if (data.accessToken) {
      sessionStorage.setItem('access_token', data.accessToken)
    }
    const u = data.user?.username ?? form.username
    loginMessage.value = `登录成功，欢迎 ${u}`
  } catch (e) {
    err.value = loginErrorMessage(e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.fintech-bg {
  background:
    radial-gradient(90% 60% at 10% 88%, rgba(27, 116, 255, 0.26), rgba(3, 9, 29, 0) 65%),
    radial-gradient(35% 40% at 84% 18%, rgba(34, 120, 255, 0.1), rgba(7, 17, 43, 0) 70%),
    linear-gradient(120deg, #030b22 0%, #041033 45%, #02071f 100%);
}

/* 小屏一屏内放不下时，仅登录卡片内部滚动，避免底部被裁切 */
.login-card-scroll {
  max-height: min(calc(100dvh - 5.5rem), 720px);
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

@media (min-width: 1024px) {
  .login-card-scroll {
    max-height: min(calc(100dvh - 6rem), 780px);
  }
}

</style>
