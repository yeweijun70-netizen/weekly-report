import XLSX from 'xlsx';
import fs from 'fs';

// 测试用例数据
const testCases = [
    // 一、用户认证模块
    { id: 'TC-001', module: '用户登录', title: '验证正确邮箱和密码登录成功', priority: 'P0', precondition: '系统已启动，存在测试账号 zhangsan@company.com/123456', steps: '1. 打开登录页面;\n2. 输入邮箱"zhangsan@company.com";\n3. 输入密码"123456";\n4. 点击登录按钮;', expected: '1. 登录成功，跳转到工作台页面;\n2. 页面右上角显示用户名"张三";\n3. localStorage存储有效token;' },
    { id: 'TC-002', module: '用户登录', title: '验证错误密码登录失败', priority: 'P0', precondition: '系统已启动，存在测试账号', steps: '1. 打开登录页面;\n2. 输入邮箱"zhangsan@company.com";\n3. 输入密码"wrongpassword";\n4. 点击登录按钮;', expected: '1. 提示"邮箱或密码错误";\n2. 停留在登录页面;' },
    { id: 'TC-003', module: '用户登录', title: '验证不存在的邮箱登录失败', priority: 'P1', precondition: '系统已启动', steps: '1. 打开登录页面;\n2. 输入邮箱"notexist@company.com";\n3. 输入密码"123456";\n4. 点击登录按钮;', expected: '1. 提示"邮箱或密码错误";\n2. 停留在登录页面;' },
    { id: 'TC-004', module: '用户登录', title: '验证禁用状态用户无法登录', priority: 'P1', precondition: '存在状态为"禁用"(status=0)的用户账号', steps: '1. 打开登录页面;\n2. 输入被禁用用户的邮箱和正确密码;\n3. 点击登录按钮;', expected: '1. 提示"邮箱或密码错误";\n2. 无法登录系统;' },
    { id: 'TC-005', module: '用户登录', title: '验证空邮箱提交校验', priority: 'P2', precondition: '系统已启动', steps: '1. 打开登录页面;\n2. 邮箱字段留空;\n3. 输入密码"123456";\n4. 点击登录按钮;', expected: '1. 提示"请输入邮箱";\n2. 无法提交表单;' },
    { id: 'TC-006', module: '用户登出', title: '验证登出功能', priority: 'P1', precondition: '用户已登录系统', steps: '1. 点击页面右上角用户头像;\n2. 点击"退出登录";', expected: '1. 跳转到登录页面;\n2. localStorage中的token已清除;' },
    { id: 'TC-007', module: '会话管理', title: '验证Token过期自动跳转登录', priority: 'P1', precondition: '用户已登录，Token已过期', steps: '1. 修改localStorage中的token为无效值;\n2. 刷新页面或发起任意API请求;', expected: '1. 自动跳转到登录页面;\n2. 提示"登录已过期，请重新登录";' },

    // 二、周报填写模块
    { id: 'TC-008', module: '周报填写', title: '验证暂存草稿功能', priority: 'P0', precondition: '用户已登录且在写周报页面，当前周无已提交周报', steps: '1. 在"本周工作完成情况"输入"完成用户模块开发";\n2. 在"下周工作计划"输入"进行测试工作";\n3. 点击"暂存"按钮;\n4. 刷新页面;', expected: '1. 提示"暂存成功";\n2. 刷新后输入内容未丢失;\n3. 周次下拉显示"草稿"标签;' },
    { id: 'TC-009', module: '周报填写', title: '验证提交周报功能', priority: 'P0', precondition: '用户已登录，已填写周报内容', steps: '1. 在"本周工作完成情况"输入"完成API接口开发";\n2. 在"下周工作计划"输入"编写单元测试";\n3. 点击"提交"按钮;\n4. 确认弹窗点击"确定";', expected: '1. 提示"提交成功";\n2. 提交和暂存按钮变为禁用状态;\n3. 显示"此周报已提交，不可修改";' },
    { id: 'TC-010', module: '周报填写', title: '验证已提交周报不可再次修改', priority: 'P0', precondition: '当前周周报已提交', steps: '1. 进入写周报页面;\n2. 选择已提交的周次;', expected: '1. "暂存"和"提交"按钮为禁用状态;\n2. 显示提示"此周报已提交，不可修改";' },
    { id: 'TC-011', module: '周报填写', title: '验证一键复制上周计划功能', priority: 'P1', precondition: '用户已登录，上周周报存在且"下周计划"字段有内容', steps: '1. 进入写周报页面;\n2. 点击"导入上周计划"按钮;', expected: '1. 提示"已导入上周计划";\n2. "本周工作完成情况"自动填充上周的"下周计划"内容;' },
    { id: 'TC-012', module: '周报填写', title: '验证导入上周计划-无上周数据', priority: 'P2', precondition: '用户已登录，上周无周报记录', steps: '1. 进入写周报页面;\n2. 点击"导入上周计划"按钮;', expected: '1. 提示"没有找到上周计划";\n2. 输入框内容不变;' },
    { id: 'TC-013', module: '周报填写', title: '验证补写历史周次周报', priority: 'P1', precondition: '用户已登录，当前为第4周，第2周无周报记录', steps: '1. 进入写周报页面;\n2. 在周次下拉框选择"第2周";\n3. 填写并提交周报;', expected: '1. 显示"正在补写第2周周报"提示;\n2. 周报提交成功;\n3. 确认弹窗显示"确认提交第2周的补写周报？";' },
    { id: 'TC-014', module: '周报填写', title: '验证周次切换保留草稿', priority: 'P1', precondition: '用户已登录，第3周有草稿，第4周无记录', steps: '1. 进入写周报页面，默认为第4周;\n2. 切换到第3周;\n3. 再切换回第4周;', expected: '1. 切换到第3周时显示草稿内容;\n2. 切换回第4周时显示空白或已保存内容;' },
    { id: 'TC-015', module: '周报填写', title: '验证富文本编辑功能', priority: 'P2', precondition: '用户已登录且在写周报页面', steps: '1. 在"本周工作"编辑器中输入文本;\n2. 选中文本，点击加粗;\n3. 添加有序列表;\n4. 保存草稿并刷新;', expected: '1. 文本样式（加粗、列表）正确保存;\n2. 刷新后样式保留;' },
    { id: 'TC-016', module: '周报填写', title: '验证需协调事项选填', priority: 'P2', precondition: '用户已登录', steps: '1. 填写"本周工作"和"下周计划";\n2. "需协调事项"留空;\n3. 点击提交;', expected: '1. 可以正常提交;\n2. 需协调事项存储为空;' },

    // 三、团队周报与权限控制模块
    { id: 'TC-017', module: '权限控制', title: '场景A：部门经理查看直属下属周报', priority: 'P0', precondition: '登录用户：部门经理张三(lisi@company.com); 下属：李四', steps: '1. 登录为部门经理张三;\n2. 进入"团队周报"页面;\n3. 在组织架构树选择自己部门;', expected: '1. 可以看到下属李四的周报列表;\n2. 点击"查看"可以查看周报详情;' },
    { id: 'TC-018', module: '权限控制', title: '场景B：部门经理跨级查看下属的下属周报', priority: 'P0', precondition: '登录：部门经理张三; 组织结构：张三 -> 李四(组长) -> 王五(员工)', steps: '1. 登录为部门经理张三;\n2. 进入"团队周报"页面;\n3. 选择包含王五的子部门;', expected: '1. 可以看到王五的周报记录;\n2. 点击"查看"可以查看王五的周报详情;' },
    { id: 'TC-019', module: '权限控制', title: '场景C：普通员工无法查看上级周报', priority: 'P0', precondition: '登录：普通员工李四; 上级：经理张三', steps: '1. 登录为普通员工李四;\n2. 进入"团队周报"页面;\n3. 搜索"张三";', expected: '1. 搜索结果中不显示张三的周报;\n2. 只能看到自己的周报相关数据;' },
    { id: 'TC-020', module: '权限控制', title: '场景D：URL ID遍历攻击防护', priority: 'P0', precondition: '登录：普通员工李四; 存在其他用户周报ID=999', steps: '1. 登录为普通员工李四;\n2. 直接访问API: GET /report/999 (其他用户的周报ID);', expected: '1. 返回无权限错误或空数据;\n2. 不能查看其他用户的周报内容;' },
    { id: 'TC-021', module: '团队周报', title: '验证部门筛选功能', priority: 'P1', precondition: '用户为部门领导，存在多个子部门', steps: '1. 进入团队周报页面;\n2. 在左侧组织架构树点击"研发部";', expected: '1. 右侧列表只显示研发部及其子部门员工的周报;\n2. 底部分页信息更新;' },
    { id: 'TC-022', module: '团队周报', title: '验证周次筛选功能', priority: 'P1', precondition: '用户已登录，存在多周周报数据', steps: '1. 进入团队周报页面;\n2. 在周次下拉框选择"第2周";', expected: '1. 列表只显示第2周的周报;\n2. 周次列显示"第2周";' },
    { id: 'TC-023', module: '团队周报', title: '验证日期范围筛选功能', priority: 'P1', precondition: '用户已登录', steps: '1. 进入团队周报页面;\n2. 选择日期范围"2026-01-01"至"2026-01-15";', expected: '1. 列表只显示该日期范围内提交的周报;' },
    { id: 'TC-024', module: '团队周报', title: '验证提交状态筛选', priority: 'P1', precondition: '用户已登录，存在已提交和未提交周报', steps: '1. 进入团队周报页面;\n2. 选择状态"已提交";', expected: '1. 列表只显示状态为"已提交"的周报;\n2. 切换为"未提交"只显示草稿;' },
    { id: 'TC-025', module: '团队周报', title: '验证关键词搜索功能', priority: 'P1', precondition: '用户已登录，存在员工"王五"', steps: '1. 进入团队周报页面;\n2. 在搜索框输入"王五";\n3. 点击搜索;', expected: '1. 列表只显示用户名包含"王五"的周报;' },
    { id: 'TC-026', module: '团队周报', title: '验证重置筛选条件功能', priority: 'P2', precondition: '用户已设置多个筛选条件', steps: '1. 设置周次、状态、关键词等筛选条件;\n2. 点击"重置"按钮;', expected: '1. 所有筛选条件清空;\n2. 列表恢复为默认全部数据;' },
    { id: 'TC-027', module: '团队周报', title: '验证分页功能', priority: 'P2', precondition: '存在超过10条周报数据', steps: '1. 进入团队周报页面;\n2. 调整每页显示数量为20;\n3. 点击下一页;', expected: '1. 每页条数变更生效;\n2. 翻页正常，数据不重复;' },
    { id: 'TC-028', module: '团队周报', title: '验证未提交周报无法查看详情', priority: 'P2', precondition: '存在状态为草稿的周报记录', steps: '1. 进入团队周报页面;\n2. 找到状态为"未提交"的记录;\n3. 尝试点击"查看"按钮;', expected: '1. "查看"按钮为禁用状态;\n2. 无法打开详情弹窗;' },

    // 四、领导点评模块
    { id: 'TC-029', module: '领导点评', title: '验证评分功能', priority: 'P0', precondition: '登录为部门领导，存在下属已提交周报', steps: '1. 进入团队周报页面;\n2. 点击下属周报"查看";\n3. 在评分区域选择4.5分;\n4. 点击"保存评价";', expected: '1. 提示"评价已保存";\n2. 列表中该周报显示评分4.5;' },
    { id: 'TC-030', module: '领导点评', title: '验证评语功能', priority: 'P0', precondition: '登录为部门领导', steps: '1. 打开下属周报详情;\n2. 输入评语"本周工作完成质量高，继续保持";\n3. 点击"保存评价";', expected: '1. 评语保存成功;\n2. 再次打开详情可看到保存的评语;' },
    { id: 'TC-031', module: '领导点评', title: '验证修改已有评分', priority: 'P1', precondition: '存在已评分的周报', steps: '1. 打开已评分的周报详情;\n2. 将评分从4.5修改为3.5;\n3. 修改评语内容;\n4. 保存;', expected: '1. 评分更新为3.5;\n2. 评语内容更新;' },
    { id: 'TC-032', module: '领导点评', title: '验证员工查看点评', priority: 'P1', precondition: '员工张三的周报已被领导点评', steps: '1. 登录为员工张三;\n2. 进入"我的周报"或工作台历史记录;\n3. 查看被点评的周报;', expected: '1. 可以看到领导的评分;\n2. 可以看到领导的评语内容;' },

    // 五、用户管理模块
    { id: 'TC-033', module: '用户管理', title: '验证新增用户', priority: 'P0', precondition: '登录为管理员', steps: '1. 进入用户管理页面;\n2. 点击"新增";\n3. 输入姓名"测试员工";\n4. 输入邮箱"test@company.com";\n5. 输入密码"123456";\n6. 选择部门和角色;\n7. 提交;', expected: '1. 提示保存成功;\n2. 列表中出现新用户;\n3. 新用户可以正常登录;' },
    { id: 'TC-034', module: '用户管理', title: '验证邮箱重复校验', priority: 'P1', precondition: '已存在邮箱zhangsan@company.com', steps: '1. 新增用户;\n2. 输入邮箱"zhangsan@company.com";\n3. 提交;', expected: '1. 提示"邮箱已存在";\n2. 无法保存;' },
    { id: 'TC-035', module: '用户管理', title: '验证编辑用户信息', priority: 'P1', precondition: '存在用户"张三"', steps: '1. 进入用户管理;\n2. 找到"张三"，点击编辑;\n3. 修改用户名为"张三三";\n4. 保存;', expected: '1. 保存成功;\n2. 列表显示更新后的用户名;' },
    { id: 'TC-036', module: '用户管理', title: '验证删除用户', priority: 'P1', precondition: '存在测试用户', steps: '1. 进入用户管理;\n2. 找到测试用户，点击删除;\n3. 确认删除;', expected: '1. 用户从列表中消失;\n2. 该用户无法再登录系统;' },
    { id: 'TC-037', module: '用户管理', title: '验证用户分页和搜索', priority: 'P2', precondition: '存在10+用户', steps: '1. 进入用户管理;\n2. 在搜索框输入"张";\n3. 点击搜索;', expected: '1. 显示用户名包含"张"的用户;\n2. 分页正常工作;' },
    { id: 'TC-038', module: '用户管理', title: '验证按部门筛选用户', priority: 'P2', precondition: '存在多个部门的用户', steps: '1. 进入用户管理;\n2. 选择部门"研发部";', expected: '1. 只显示研发部及其子部门的用户;' },

    // 六、角色管理模块
    { id: 'TC-039', module: '角色管理', title: '验证新增角色', priority: 'P1', precondition: '登录为管理员', steps: '1. 进入角色管理;\n2. 点击"新增";\n3. 输入角色名"实习生";\n4. 输入角色编码"intern";\n5. 保存;', expected: '1. 角色创建成功;\n2. 角色列表显示新角色;' },
    { id: 'TC-040', module: '角色管理', title: '验证角色编码重复校验', priority: 'P1', precondition: '已存在编码"admin"', steps: '1. 新增角色;\n2. 输入角色编码"admin";\n3. 保存;', expected: '1. 提示"角色编码已存在";\n2. 无法保存;' },
    { id: 'TC-041', module: '角色管理', title: '验证配置角色权限', priority: 'P1', precondition: '存在角色"组长"', steps: '1. 进入角色管理;\n2. 点击"组长"角色的权限配置;\n3. 勾选"团队周报查看"权限;\n4. 保存;', expected: '1. 权限配置保存成功;\n2. 该角色用户登录后可见相应菜单;' },
    { id: 'TC-042', module: '角色管理', title: '验证删除角色', priority: 'P2', precondition: '存在未分配用户的测试角色', steps: '1. 找到测试角色;\n2. 点击删除;\n3. 确认;', expected: '1. 角色删除成功;\n2. 角色从列表消失;' },

    // 七、部门管理模块
    { id: 'TC-043', module: '部门管理', title: '验证获取部门树', priority: 'P1', precondition: '存在多级部门结构', steps: '1. 进入团队周报页面;\n2. 查看左侧组织架构树;', expected: '1. 正确展示部门层级结构;\n2. 子部门缩进显示;' },
    { id: 'TC-044', module: '部门管理', title: '验证新增部门', priority: 'P1', precondition: '登录为管理员', steps: '1. 进入部门管理;\n2. 点击新增;\n3. 输入部门名"测试部";\n4. 选择上级部门;\n5. 保存;', expected: '1. 部门创建成功;\n2. 在部门树中正确位置显示;' },
    { id: 'TC-045', module: '部门管理', title: '验证删除部门', priority: 'P2', precondition: '存在无子部门的测试部门', steps: '1. 找到测试部门;\n2. 点击删除;\n3. 确认;', expected: '1. 部门删除成功;\n2. 部门树中不再显示;' },

    // 八、统计分析模块
    { id: 'TC-046', module: '统计分析', title: '验证提交率统计', priority: 'P1', precondition: '存在当周周报数据', steps: '1. 进入统计分析页面;\n2. 选择年份和周次;\n3. 查看提交率饼图;', expected: '1. 饼图正确显示已提交/未提交比例;\n2. 数据与实际情况一致;' },
    { id: 'TC-047', module: '统计分析', title: '验证评分趋势图', priority: 'P1', precondition: '存在多周评分数据', steps: '1. 进入统计分析页面;\n2. 查看评分趋势折线图;', expected: '1. 折线图显示近期周次的平均评分趋势;\n2. 坐标轴标签正确;' },
    { id: 'TC-048', module: '统计分析', title: '验证工作台统计卡片', priority: 'P2', precondition: '用户已登录', steps: '1. 登录后进入工作台;\n2. 查看顶部统计卡片;', expected: '1. 显示本周周报状态;\n2. 显示待处理事项数量;' },

    // 九、数据边界与异常场景
    { id: 'TC-049', module: '数据边界', title: '验证周报内容超长文本', priority: 'P2', precondition: '用户已登录', steps: '1. 在"本周工作"输入超过10000字符的内容;\n2. 保存提交;', expected: '1. 根据数据库字段限制，提示长度超限或截断保存;' },
    { id: 'TC-050', module: '数据边界', title: '验证第1周无法导入上周计划', priority: 'P2', precondition: '当前为年度第1周', steps: '1. 进入写周报页面;\n2. 选择第1周;\n3. 点击"导入上周计划";', expected: '1. 提示"没有找到上周计划";\n2. 能正确处理跨年边界(上年第52周);' },
    { id: 'TC-051', module: '数据边界', title: '验证评分边界值', priority: 'P2', precondition: '登录为领导', steps: '1. 打开下属周报;\n2. 尝试输入评分0;\n3. 尝试输入评分5;\n4. 尝试输入评分5.5;', expected: '1. 评分0和5可正常保存;\n2. 超出范围的值被限制或提示;' },
    { id: 'TC-052', module: '异常场景', title: '验证网络断开时的提示', priority: 'P2', precondition: '用户已登录', steps: '1. 断开网络;\n2. 点击提交周报;', expected: '1. 提示"网络连接失败";\n2. 不丢失已填写内容;' },
    { id: 'TC-053', module: '异常场景', title: '验证并发提交防护', priority: 'P2', precondition: '用户在两个浏览器标签登录', steps: '1. 标签A填写周报准备提交;\n2. 标签B先提交成功;\n3. 标签A再点击提交;', expected: '1. 能正确处理，不产生重复记录;\n2. 第二次提交提示已存在或更新;' },
];

// 创建工作簿
const wb = XLSX.utils.book_new();

// 创建工作表数据
const wsData = [
    ['用例ID', '用例模块', '用例标题', '优先级', '前置条件', '测试步骤', '预期结果'],
    ...testCases.map(tc => [tc.id, tc.module, tc.title, tc.priority, tc.precondition, tc.steps, tc.expected])
];

// 创建工作表
const ws = XLSX.utils.aoa_to_sheet(wsData);

// 设置列宽
ws['!cols'] = [
    { wch: 10 },   // 用例ID
    { wch: 12 },   // 用例模块
    { wch: 35 },   // 用例标题
    { wch: 8 },    // 优先级
    { wch: 40 },   // 前置条件
    { wch: 50 },   // 测试步骤
    { wch: 50 },   // 预期结果
];

// 添加工作表到工作簿
XLSX.utils.book_append_sheet(wb, ws, '测试用例');

// 写入文件
XLSX.writeFile(wb, '../测试用例.xlsx');

console.log('Excel文件已生成: 测试用例.xlsx');
console.log(`共 ${testCases.length} 条测试用例`);
