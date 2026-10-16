const { createApp, ref, reactive } = Vue;

if (localStorage.getItem('username')) {
    window.location.href = '/main.html';
}

const api = axios.create({ withCredentials: true });
api.interceptors.response.use(
    res => res.data,
    err => {
        const data = err.response?.data;
        return Promise.reject(data || { msg: '请求失败' });
    }
);

const app = createApp({
    setup() {
        const form = reactive({ username: '', password: '', confirmPassword: '' });
        const loading = ref(false);
        const formRef = ref(null);
        const rules = {
            username: [
                { required: true, message: '请输入用户名', trigger: 'blur' },
                { min: 3, max: 50, message: '用户名长度3-50', trigger: 'blur' }
            ],
            password: [
                { required: true, message: '请输入密码', trigger: 'blur' },
                { min: 6, message: '密码至少6位', trigger: 'blur' }
            ],
            confirmPassword: [
                { required: true, message: '请确认密码', trigger: 'blur' },
                { validator: (rule, val, cb) => val === form.password ? cb() : cb(new Error('两次密码不一致')), trigger: 'blur' }
            ]
        };

        function handleRegister() {
            if (!formRef.value) return;
            formRef.value.validate(valid => {
                if (!valid) return;
                loading.value = true;
                const params = new URLSearchParams();
                params.append('username', form.username);
                params.append('password', form.password);
                api.post('/api/user/register', params)
                    .then(() => {
                        ElementPlus.ElMessage.success('注册成功，请登录');
                        window.location.href = '/login.html';
                    })
                    .catch(err => { ElementPlus.ElMessage.error(err.msg || '注册失败'); })
                    .finally(() => { loading.value = false; });
            });
        }

        return { form, loading, formRef, rules, handleRegister };
    }
});

app.use(ElementPlus);
app.mount('#app');