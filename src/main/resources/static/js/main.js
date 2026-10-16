const { createApp, ref, reactive, onMounted, nextTick } = Vue;

if (!localStorage.getItem('username')) {
    window.location.href = '/login.html';
}

const api = axios.create({ withCredentials: true });
api.interceptors.response.use(
    res => res.data,
    err => {
        const data = err.response?.data;
        if (err.response?.status === 401) {
            localStorage.removeItem('username');
            window.location.href = '/login.html';
        }
        return Promise.reject(data || { msg: '请求失败' });
    }
);

// Statistics data
const stats = reactive({
    totalEmployees: 0,
    totalDepartments: 0,
    newThisMonth: 0,
    genderDistribution: { '男': 0, '女': 0 },
    departmentDistribution: {}
});

async function loadStatistics() {
    try {
        api.get('/api/staff/statistics').then(res => {
            if (res.data) {
                Object.assign(stats, res.data);
                nextTick(() => initCharts());
            }
        }).catch(() => {});
    } catch(e) {}
}

function initCharts() {
    // Department distribution bar chart
    const deptDom = document.getElementById('chart-dept');
    if (deptDom) {
        const deptChart = echarts.init(deptDom);
        const deptData = stats.departmentDistribution;
        const deptNames = Object.keys(deptData);
        const deptValues = Object.values(deptData);
        deptChart.setOption({
            tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
            grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
            xAxis: { type: 'value' },
            yAxis: { type: 'category', data: deptNames.length > 0 ? deptNames : ['暂无数据'] },
            series: [{
                data: deptValues.length > 0 ? deptValues : [0],
                type: 'bar',
                itemStyle: { color: '#1890ff', borderRadius: [0, 4, 4, 0] },
                barWidth: 20
            }]
        });
        window.addEventListener('resize', () => deptChart.resize());
    }

    // Gender distribution donut chart
    const genderDom = document.getElementById('chart-gender');
    if (genderDom) {
        const genderChart = echarts.init(genderDom);
        const genderData = stats.genderDistribution;
        const total = (genderData['男'] || 0) + (genderData['女'] || 0);
        genderChart.setOption({
            tooltip: { trigger: 'item' },
            legend: { bottom: 0 },
            series: [{
                type: 'pie',
                radius: ['55%', '75%'],
                center: ['50%', '45%'],
                label: { show: true, formatter: '{b}\n{d}%' },
                data: total > 0 ? [
                    { value: genderData['男'] || 0, name: '男', itemStyle: { color: '#1890ff' } },
                    { value: genderData['女'] || 0, name: '女', itemStyle: { color: '#ff85c0' } }
                ] : [{ value: 1, name: '暂无数据', itemStyle: { color: '#d9d9d9' } }],
                emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.5)' } }
            }]
        });
        window.addEventListener('resize', () => genderChart.resize());
    }
}

const app = createApp({
    setup() {
        const currentUser = ref(localStorage.getItem('username') || '');

        const queryForm = reactive({
            name: '', gender: '', deptName: '', education: '', workStatus: '', nativePlace: '',
            page: 1, size: 10
        });
        const staffList = ref([]);
        const total = ref(0);
        const tableLoading = ref(false);
        const selectedIds = ref([]);

        const dialogVisible = ref(false);
        const dialogTitle = ref('');
        const isEdit = ref(false);
        const staffForm = reactive({
            id: null, name: '', gender: '男', age: null, deptName: '', phone: '',
            education: '', workStatus: '在职', idCare: '', nativePlace: '', address: '',
            bankCard: '', basicSalary: 0
        });
        const staffFormRef = ref(null);
        const submitLoading = ref(false);
        const staffRules = {
            name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
            gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
            deptName: [{ required: true, message: '请输入岗位', trigger: 'blur' }],
            phone: [
                { required: true, message: '请输入手机号', trigger: 'blur' },
                { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
            ],
            education: [{ required: true, message: '请输入学历', trigger: 'blur' }],
            idCare: [{ required: true, message: '请输入身份证号', trigger: 'blur' }]
        };

        function handleLogout() {
            api.post('/api/user/logout').catch(() => {}).finally(() => {
                localStorage.removeItem('username');
                window.location.href = '/login.html';
            });
        }

        function handleQuery() {
            tableLoading.value = true;
            const params = new URLSearchParams();
            Object.entries(queryForm).forEach(([k, v]) => { if (v !== '' && v !== null) params.append(k, v); });
            api.post('/api/staff/page', params)
                .then(res => {
                    staffList.value = res.data?.records || [];
                    total.value = res.data?.total || 0;
                })
                .catch(err => { ElementPlus.ElMessage.error(err.msg || '查询失败'); })
                .finally(() => { tableLoading.value = false; });
        }

        function resetQuery() {
            queryForm.name = '';
            queryForm.gender = '';
            queryForm.deptName = '';
            queryForm.education = '';
            queryForm.workStatus = '';
            queryForm.nativePlace = '';
            queryForm.page = 1;
        }

        function handleSelectionChange(rows) {
            selectedIds.value = rows.map(r => r.id);
        }

        function openAddDialog() {
            isEdit.value = false;
            dialogTitle.value = '新增员工';
            Object.assign(staffForm, {
                id: null, name: '', gender: '男', age: null, deptName: '', phone: '',
                education: '', workStatus: '在职', idCare: '', nativePlace: '', address: '',
                bankCard: '', basicSalary: 0
            });
            dialogVisible.value = true;
        }

        function openEditDialog(row) {
            isEdit.value = true;
            dialogTitle.value = '编辑员工';
            Object.assign(staffForm, { ...row });
            dialogVisible.value = true;
        }

        function handleSubmit() {
            if (!staffFormRef.value) return;
            staffFormRef.value.validate(valid => {
                if (!valid) return;
                submitLoading.value = true;
                const request = isEdit.value ? api.put('/api/staff', staffForm) : api.post('/api/staff', staffForm);
                request.then(() => {
                    ElementPlus.ElMessage.success(isEdit.value ? '修改成功' : '添加成功');
                    dialogVisible.value = false;
                    handleQuery();
                    loadStatistics();
                }).catch(err => { ElementPlus.ElMessage.error(err.msg || '操作失败'); })
                .finally(() => { submitLoading.value = false; });
            });
        }

        function handleDelete(row) {
            ElementPlus.ElMessageBox.confirm('确认删除该员工？', '提示', { type: 'warning' })
                .then(() => {
                    api.delete('/api/staff/' + row.id)
                        .then(() => {
                            ElementPlus.ElMessage.success('删除成功');
                            handleQuery();
                            loadStatistics();
                        })
                        .catch(err => { ElementPlus.ElMessage.error(err.msg || '删除失败'); });
                }).catch(() => {});
        }

        function handleBatchDelete() {
            ElementPlus.ElMessageBox.confirm('确认删除选中的' + selectedIds.value.length + ' 条记录？', '提示', { type: 'warning' })
                .then(() => {
                    api.delete('/api/staff/batch', { data: selectedIds.value })
                        .then(() => {
                            ElementPlus.ElMessage.success('批量删除成功');
                            handleQuery();
                            loadStatistics();
                        })
                        .catch(err => { ElementPlus.ElMessage.error(err.msg || '删除失败'); });
                }).catch(() => {});
        }

        onMounted(() => {
            handleQuery();
            loadStatistics();
        });

        return {
            currentUser, queryForm, staffList, total, tableLoading, selectedIds,
            dialogVisible, dialogTitle, staffForm, staffFormRef, submitLoading, staffRules, stats,
            handleLogout, handleQuery, resetQuery, handleSelectionChange,
            openAddDialog, openEditDialog, handleSubmit, handleDelete, handleBatchDelete, handleExport
        };
    }
});

app.use(ElementPlus);
app.mount('#app');

        function handleExport() {
            window.open('/api/staff/export', '_blank');
        }
