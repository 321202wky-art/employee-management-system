const { createApp, ref, reactive, onMounted } = Vue;

if (localStorage.getItem('username')) {
    window.location.href = '/main.html';
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

function setCaptchaImage(dataUrl) {
    const el = document.getElementById('captcha-image');
    if (el) {
        if (dataUrl) {
            el.style.backgroundImage = 'url(' + dataUrl + ')';
            el.innerHTML = '';
        } else {
            el.style.backgroundImage = '';
            el.innerHTML = '加载中...';
        }
    }
}

const app = createApp({
    setup() {
        const form = reactive({ username: '', password: '', captchaCode: '' });
        const loading = ref(false);
        const formRef = ref(null);
        const captchaUuid = ref('');

        function loadCaptcha() {
            setCaptchaImage('');
            api.get('/api/captcha').then(res => {
                if (res.data && res.data.image) {
                    setCaptchaImage('data:image/png;base64,' + res.data.image);
                    captchaUuid.value = res.data.uuid;
                    form.captchaCode = '';
                }
            }).catch(() => {
                setTimeout(loadCaptcha, 3000);
            });
        }

        onMounted(() => { loadCaptcha(); });

        function handleLogin() {
            if (!formRef.value) return;
            formRef.value.validate(valid => {
                if (!valid) return;
                loading.value = true;
                const params = new URLSearchParams();
                params.append('username', form.username);
                params.append('password', form.password);
                params.append('captchaUuid', captchaUuid.value);
                params.append('captchaCode', form.captchaCode);
                api.post('/api/user/login', params)
                    .then(res => {
                        localStorage.setItem('username', form.username);
                        window.location.href = '/loading.html';
                    })
                    .catch(err => {
                        loadCaptcha();
                        form.captchaCode = '';
                        ElementPlus.ElMessage.error(err.msg || '登录失败');
                    })
                    .finally(() => { loading.value = false; });
            });
        }

        return { form, loading, formRef, captchaUuid, handleLogin, loadCaptcha };
    }
});

app.use(ElementPlus);
app.mount('#app');