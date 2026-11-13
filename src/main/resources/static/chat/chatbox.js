(() => {
    'use strict';
    const root = document.getElementById('metro-ai');
    if (!root || root.dataset.ready) return;
    root.dataset.ready = 'true';
    const panel = document.getElementById('metro-ai-panel');
    const launcher = document.getElementById('metro-ai-launcher');
    const log = document.getElementById('metro-ai-messages');
    const form = document.getElementById('metro-ai-form');
    const input = document.getElementById('metro-ai-input');
    const send = document.getElementById('metro-ai-send');
    const newChat = document.getElementById('metro-ai-new');
    const status = document.getElementById('metro-ai-status');
    const prefix = 'metro-ai-history:';
    const storageKey = prefix + root.dataset.sessionKey;
    let messages = [];
    let busy = false;
    const welcome = 'Xin chào! Mình là Metro AI Assistant. Mình có thể hướng dẫn đăng nhập, tìm chuyến tàu và mua vé trên website. Bạn cần hỗ trợ gì?\nMình chưa có dữ liệu thực tế để xác nhận giờ tàu, giá vé hoặc thông tin đơn hàng.';

    function bubble(role, text) {
        const item = document.createElement('div');
        item.className = 'metro-ai-message metro-ai-' + role;
        const author = document.createElement('span');
        author.textContent = role === 'user' ? 'Bạn' : 'Metro AI';
        const content = document.createElement('p');
        content.textContent = text; // Never interpret user/provider text as HTML.
        item.append(author, content);
        log.append(item);
        log.scrollTop = log.scrollHeight;
    }
    function save() {
        messages = messages.slice(-40);
        try { sessionStorage.setItem(storageKey, JSON.stringify(messages)); } catch (_) { /* Memory-only fallback. */ }
    }
    function append(role, text) {
        messages.push({role, text});
        save();
        bubble(role, text);
    }
    function resetView() {
        messages = [];
        save();
        log.replaceChildren();
        bubble('assistant', welcome);
    }
    try {
        // Remove the previous identity's history after login, logout or a new server session.
        for (const key of Object.keys(sessionStorage)) {
            if (key.startsWith(prefix) && key !== storageKey) sessionStorage.removeItem(key);
        }
        const saved = JSON.parse(sessionStorage.getItem(storageKey) || '[]');
        if (Array.isArray(saved)) messages = saved.filter(item => item && ['user', 'assistant', 'error'].includes(item.role)
            && typeof item.text === 'string' && item.text.length <= 8000).slice(-40);
    } catch (_) { messages = []; }
    bubble('assistant', welcome);
    messages.forEach(item => bubble(item.role, item.text));

    function toggle(open) {
        panel.hidden = !open;
        launcher.setAttribute('aria-expanded', String(open));
        if (open) { log.scrollTop = log.scrollHeight; input.focus(); }
        else launcher.focus();
    }
    function setBusy(value, text = '') {
        busy = value;
        send.disabled = value;
        newChat.disabled = value;
        input.readOnly = value;
        status.textContent = text;
        log.setAttribute('aria-busy', String(value));
    }
    async function request(url, body) {
        const controller = new AbortController();
        const timer = setTimeout(() => controller.abort(), 65000);
        try {
            const response = await fetch(url, {
                method: 'POST', credentials: 'same-origin', redirect: 'error', cache: 'no-store',
                headers: {'Content-Type': 'application/json', 'Accept': 'application/json',
                    [root.dataset.csrfHeader]: root.dataset.csrfToken},
                body: JSON.stringify(body), signal: controller.signal
            });
            if (response.status === 403) throw new Error('Phiên bảo mật đã thay đổi hoặc hết hạn. Vui lòng tải lại trang rồi thử lại.');
            const data = response.status === 204 ? {} : await response.json().catch(() => ({}));
            if (!response.ok) {
                const fallback = response.status === 429 ? 'Bạn gửi tin nhắn hơi nhanh. Vui lòng chờ một phút rồi thử lại.'
                    : 'Metro AI tạm thời gặp sự cố. Vui lòng thử lại sau.';
                throw new Error(typeof data.error === 'string' ? data.error : fallback);
            }
            return data;
        } catch (error) {
            if (error.name === 'AbortError') throw new Error('Metro AI chưa phản hồi kịp. Vui lòng thử lại sau.');
            if (error instanceof TypeError) throw new Error('Không thể kết nối Metro AI. Vui lòng kiểm tra mạng và thử lại.');
            throw error;
        } finally { clearTimeout(timer); }
    }
    launcher.addEventListener('click', () => toggle(panel.hidden));
    document.getElementById('metro-ai-close').addEventListener('click', () => toggle(false));
    document.getElementById('metro-ai-minimize').addEventListener('click', () => toggle(false));
    root.addEventListener('keydown', event => {
        if (event.key === 'Escape' && !panel.hidden) { event.preventDefault(); toggle(false); }
    });
    input.addEventListener('keydown', event => {
        if (event.key === 'Enter' && !event.shiftKey && !event.isComposing) {
            event.preventDefault();
            if (!busy && input.value.trim()) form.requestSubmit();
        }
    });
    form.addEventListener('submit', async event => {
        event.preventDefault();
        const message = input.value.trim();
        if (busy || !message || message.length > 2000) return;
        append('user', message);
        input.value = '';
        setBusy(true, 'Metro AI đang trả lời...');
        try {
            const data = await request(root.dataset.messageUrl, {message});
            if (typeof data.reply !== 'string' || !data.reply.trim()) throw new Error('Metro AI chưa có câu trả lời. Vui lòng thử lại.');
            append('assistant', data.reply);
        } catch (error) { append('error', error.message); }
        finally { setBusy(false); if (!panel.hidden) input.focus(); }
    });
    newChat.addEventListener('click', async () => {
        if (busy) return;
        setBusy(true, 'Đang tạo cuộc trò chuyện mới...');
        try { await request(root.dataset.resetUrl, {}); resetView(); }
        catch (error) { append('error', error.message); }
        finally { setBusy(false); input.focus(); }
    });
    // Re-render from the server after back/forward cache restores an old identity or CSRF token.
    window.addEventListener('pageshow', event => { if (event.persisted) window.location.reload(); });
})();
