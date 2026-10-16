const { createApp, ref, reactive, computed, nextTick } = Vue;

const api = axios.create({ withCredentials: true });
api.interceptors.response.use(
    res => res.data,
    err => {
        const data = err.response?.data;
        if (err.response?.status === 401) {
            localStorage.removeItem('username');
            window.location.reload();
        }
        return Promise.reject(data || { msg: '����ʧ��' });
    }
);

const app = createApp({
    setup() {
        const currentView = ref(localStorage.getItem('username') ? 'main' : 'login');
        const currentUser = ref(localStorage.getItem('username') || '');

        /* ��¼���� */
        const loginForm = reactive({ username: '', password: '' });
        const loginLoading = ref(false);
        const loginRules = {
            username: [{ required: true, message: '�������û���', trigger: 'blur' }],
            password: [{ required: true, message: '����������', trigger: 'blur' }]
        };

        /* ע����� */
        const registerForm = reactive({ username: '', password: '', confirmPassword: '' });
        const registerLoading = ref(false);
        const registerRules = {
            username: [
                { required: true, message: '�������û���', trigger: 'blur' },
                { min: 3, max: 50, message: '�û�������3-50', trigger: 'blur' }
            ],
            password: [
                { required: true, message: '����������', trigger: 'blur' },
                { min: 6, message: '��������6λ', trigger: 'blur' }
            ],
            confirmPassword: [
                { required: true, message: '��ȷ������', trigger: 'blur' },
                { validator: (rule, val, cb) => val === registerForm.password ? cb() : cb(new Error('�������벻һ��')), trigger: 'blur' }
            ]
        };

        /* ��ѯ����� */
        const queryForm = reactive({
            name: '', gender: '', deptName: '', title: '', workStatus: '',
            entryTimeStart: null, entryTimeEnd: null, page: 1, size: 10
        });
        const teacherList = ref([]);
        const total = ref(0);
        const tableLoading = ref(false);
        const selectedIds = ref([]);

        /* ����/�༭���� */
        const dialogVisible = ref(false);
        const dialogTitle = ref('');
        const isEdit = ref(false);
        const teacherForm = reactive({
            id: null, name: '', gender: '��', deptName: '', title: '',
            phone: '', entryTime: null, workStatus: '��ְ'
        });
        const teacherFormRef = ref(null);
        const submitLoading = ref(false);
        const teacherRules = {
            name: [{ required: true, message: '����������', trigger: 'blur' }],
            phone: [{ pattern: /^1\d{10}$/, message: '�ֻ��Ÿ�ʽ����ȷ', trigger: 'blur' }],
            entryTime: [{ required: true, message: '��ѡ����ְʱ��', trigger: 'change' }]
        };

        function handleLogin() {
            loginLoading.value = true;
            const params = new URLSearchParams();
            params.append('username', loginForm.username);
            params.append('password', loginForm.password);
            api.post('/api/auth/login', params)
                .then(() => {
                    localStorage.setItem('username', loginForm.username);
                    currentUser.value = loginForm.username;
                    currentView.value = 'main';
                    handleQuery();
                })
                .catch(err => { ElementPlus.ElMessage.error(err.msg || '��¼ʧ��'); })
                .finally(() => { loginLoading.value = false; });
        }

        function handleRegister() {
            registerLoading.value = true;
            const params = new URLSearchParams();
            params.append('username', registerForm.username);
            params.append('password', registerForm.password);
            api.post('/api/auth/register', params)
                .then(() => {
                    ElementPlus.ElMessage.success('ע��ɹ������¼');
                    currentView.value = 'login';
                })
                .catch(err => { ElementPlus.ElMessage.error(err.msg || 'ע��ʧ��'); })
                .finally(() => { registerLoading.value = false; });
        }

        function handleLogout() {
            api.post('/api/auth/logout').catch(() => {}).finally(() => {
                localStorage.removeItem('username');
                currentView.value = 'login';
            });
        }

        function handleQuery() {
            tableLoading.value = true;
            const params = new URLSearchParams();
            Object.entries(queryForm).forEach(([k, v]) => { if (v !== '' && v !== null) params.append(k, v); });
            api.post('/api/staff/page', params)
                .then(res => {
                    teacherList.value = res.data.records;
                    total.value = res.data.total;
                })
                .catch(err => { ElementPlus.ElMessage.error(err.msg || '��ѯʧ��'); })
                .finally(() => { tableLoading.value = false; });
        }

        function resetQuery() {
            queryForm.name = '';
            queryForm.gender = '';
            queryForm.deptName = '';
            queryForm.title = '';
            queryForm.workStatus = '';
            queryForm.entryTimeStart = null;
            queryForm.entryTimeEnd = null;
            queryForm.page = 1;
        }

        function handleSelectionChange(rows) {
            selectedIds.value = rows.map(r => r.id);
        }

        function openAddDialog() {
            isEdit.value = false;
            dialogTitle.value = '����Ա��';
            Object.assign(teacherForm, {
                id: null, name: '', gender: '��', deptName: '', title: '',
                phone: '', entryTime: null, workStatus: '��ְ'
            });
            dialogVisible.value = true;
        }

        function openEditDialog(row) {
            isEdit.value = true;
            dialogTitle.value = '�༭Ա��';
            Object.assign(teacherForm, { ...row });
            dialogVisible.value = true;
        }

        function handleSubmit() {
            if (!teacherFormRef.value) return;
            teacherFormRef.value.validate(valid => {
                if (!valid) return;
                submitLoading.value = true;
                const request = isEdit.value ? api.put('/api/staff', teacherForm) : api.post('/api/staff', teacherForm);
                request.then(() => {
                    ElementPlus.ElMessage.success(isEdit.value ? '�޸ĳɹ�' : '���ӳɹ�');
                    dialogVisible.value = false;
                    handleQuery();
                }).catch(err => { ElementPlus.ElMessage.error(err.msg || '����ʧ��'); })
                .finally(() => { submitLoading.value = false; });
            });
        }

        function handleDelete(row) {
            ElementPlus.ElMessageBox.confirm('ȷ��ɾ����Ա����', '��ʾ', { type: 'warning' })
                .then(() => {
                    api.delete('/api/staff/' + row.id)
                        .then(() => {
                            ElementPlus.ElMessage.success('ɾ���ɹ�');
                            handleQuery();
                        })
                        .catch(err => { ElementPlus.ElMessage.error(err.msg || 'ɾ��ʧ��'); });
                }).catch(() => {});
        }

        function handleBatchDelete() {
            ElementPlus.ElMessageBox.confirm('ȷ��ɾ��ѡ�е� ' + selectedIds.value.length + ' ����¼��', '��ʾ', { type: 'warning' })
                .then(() => {
                    api.delete('/api/staff/batch', { data: selectedIds.value })
                        .then(() => {
                            ElementPlus.ElMessage.success('����ɾ���ɹ�');
                            handleQuery();
                        })
                        .catch(err => { ElementPlus.ElMessage.error(err.msg || 'ɾ��ʧ��'); });
                }).catch(() => {});
        }

        if (currentView.value === 'main') {
            setTimeout(() => handleQuery(), 0);
        }

        return {
            currentView, currentUser,
            loginForm, loginLoading, loginRules,
            registerForm, registerLoading, registerRules,
            queryForm, teacherList, total, tableLoading, selectedIds,
            dialogVisible, dialogTitle, teacherForm, teacherFormRef, submitLoading, teacherRules,
            handleLogin, handleRegister, handleLogout,
            handleQuery, resetQuery, handleSelectionChange,
            openAddDialog, openEditDialog, handleSubmit, handleDelete, handleBatchDelete
        };
    }
});

app.use(ElementPlus);
app.mount('#app');
