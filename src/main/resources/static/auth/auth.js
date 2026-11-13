document.querySelectorAll('[data-password-toggle]').forEach(button => {
    button.addEventListener('click', () => {
        const input = document.getElementById(button.dataset.passwordToggle);
        const visible = input.type === 'password';
        input.type = visible ? 'text' : 'password';
        button.textContent = visible ? 'Ẩn mật khẩu' : 'Hiện mật khẩu';
        button.setAttribute('aria-pressed', String(visible));
    });
});
const password = document.getElementById('password');
const confirmPassword = document.getElementById('confirmPassword');
if (password && confirmPassword) {
    const validate = () => {
        const policy = /\p{L}/u.test(password.value) && /\p{Nd}/u.test(password.value)
            && new TextEncoder().encode(password.value).length <= 72;
        password.setCustomValidity(policy ? '' : 'Mật khẩu phải có chữ và số, tối đa 72 byte UTF-8.');
        confirmPassword.setCustomValidity(password.value === confirmPassword.value ? '' : 'Xác nhận mật khẩu không khớp.');
    };
    password.addEventListener('input', validate);
    confirmPassword.addEventListener('input', validate);
}
document.querySelectorAll('[data-auth-form]').forEach(form => {
    form.addEventListener('submit', () => {
        const submit = form.querySelector('[type="submit"]');
        submit.disabled = true;
        submit.textContent = 'Đang xử lý…';
    });
});
window.addEventListener('pageshow', () => {
    document.querySelectorAll('[data-auth-form] [type="submit"]').forEach(button => {
        button.disabled = false;
        button.textContent = button.form.action.endsWith('/register') ? 'Đăng ký' : 'Đăng nhập';
    });
});
