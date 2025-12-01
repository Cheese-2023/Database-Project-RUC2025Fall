<template>
  <div class="login-page">
    <div class="login-card">
      <div class="brand">
        <i class="i-carbon-warning"></i>
        <h2>县域风险监控平台</h2>
        <p>请使用账号密码登录</p>
      </div>
      <div class="form">
        <div class="form-item">
          <label>用户名</label>
          <input v-model="username" placeholder="请输入用户名" />
        </div>
        <div class="form-item">
          <label>密码</label>
          <input v-model="password" type="password" placeholder="请输入密码" />
        </div>
        <button class="primary" @click="login">登录</button>
        <p v-if="message" class="message">{{ message }}</p>
      </div>
      <div v-if="role" class="role-info">
        <span>当前角色: {{ role }}</span>
        <button v-if="role === 'ADMIN'" class="ghost" @click="adminAction">
          管理员操作
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

const username = ref('')
const password = ref('')
const message = ref('')
const role = ref('')

const login = async () => {
  try {
    const res = await axios.post('/api/user/login', null, {
      params: { username: username.value, password: password.value }
    })
    if (res.data.success) {
      // 保存登录状态（这里简单用 localStorage 模拟）
      role.value = res.data.role
      localStorage.setItem('userRole', res.data.role)
      localStorage.setItem('username', res.data.username)
      localStorage.setItem('isLoggedIn', 'true')
      message.value = '登录成功，正在跳转...'

      // 跳转到风险监控大屏
      router.push('/dashboard')
    } else {
      message.value = res.data.message || '用户名或密码错误'
    }
  } catch (err) {
    message.value = '登录失败，请稍后重试'
  }
}

const adminAction = async () => {
  try {
    const res = await axios.post('/api/user/admin-action', null, {
      headers: { role: role.value }
    })
    alert(res.data)
  } catch (err) {
    alert('操作失败: ' + err.response.data)
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at top, #1f3b73, #0b1b33);
  padding: 40px 16px;
}

.login-card {
  width: 100%;
  max-width: 420px;
  padding: 32px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 25px 45px rgba(15, 33, 67, 0.3);
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.brand {
  text-align: center;
  color: #102544;
}

.brand i {
  font-size: 40px;
  display: inline-block;
  margin-bottom: 8px;
}

.brand h2 {
  margin: 0;
  font-size: 20px;
}

.brand p {
  margin-top: 8px;
  color: #5c6b80;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: #1a2942;
  font-size: 14px;
}

input {
  border: 1px solid #d5ddec;
  border-radius: 10px;
  padding: 12px 14px;
  font-size: 14px;
  transition: border 0.2s, box-shadow 0.2s;
}

input:focus {
  outline: none;
  border-color: #3981ff;
  box-shadow: 0 0 0 3px rgba(57, 129, 255, 0.2);
}

button {
  border: none;
  border-radius: 12px;
  padding: 12px 18px;
  font-size: 15px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

button:active {
  transform: scale(0.98);
}

.primary {
  background: linear-gradient(120deg, #3981ff, #4bc0ff);
  color: #fff;
  box-shadow: 0 12px 20px rgba(57, 129, 255, 0.35);
}

.ghost {
  background: #f4f8ff;
  color: #2763c4;
}

.message {
  color: #d04c4c;
  text-align: center;
  font-size: 14px;
}

.role-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 16px;
  border-top: 1px solid #e5ecfb;
  color: #1f3b73;
  font-size: 14px;
}

@media (max-width: 480px) {
  .login-card {
    padding: 24px 20px;
  }
}
</style>
