const express = require('express');
const path = require('path');

const app = express();
const PORT = 3000;

// 解析 JSON 请求体（登录时前端会发 JSON）
app.use(express.json());

// 静态文件：把 public 目录下的文件直接提供给浏览器
app.use(express.static(path.join(__dirname, 'public')));

// 模拟用户表（实际项目会查数据库）
const users = [
  { username: 'admin', password: '123456' },
  { username: 'test',  password: '123456' }
];

// 登录接口：POST /api/login
app.post('/api/login', (req, res) => {
  const { username, password } = req.body || {};
  const user = users.find(u => u.username === username && u.password === password);
  if (!user) {
    return res.status(401).json({ success: false, message: '用户名或密码错误' });
  }
  res.json({ success: true, message: '登录成功', username: user.username });
});

// 首页：返回登录页（即 public/index.html）
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

app.listen(PORT, () => {
  console.log('迷你登录系统已启动: http://localhost:' + PORT);
});
