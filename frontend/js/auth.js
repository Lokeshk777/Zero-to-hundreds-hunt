/* ====================================================================
   ZERO TO HUNDRED'S HUNT - AUTHENTICATION LOGIC
   Handles Signup, Login, Password Reset, and Password Strength
   ==================================================================== */

document.addEventListener('DOMContentLoaded', () => {
  initPasswordToggles();
  initPasswordStrength();
  initSignupForm();
  initLoginForm();
  initForgotPasswordForm();
  initResetPasswordForm();
});

// Password visibility toggles
function initPasswordToggles() {
  document.querySelectorAll('.input-toggle-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const targetId = btn.getAttribute('data-target');
      const input = document.getElementById(targetId);
      if (!input) return;

      if (input.type === 'password') {
        input.type = 'text';
        btn.innerHTML = '👁️';
      } else {
        input.type = 'password';
        btn.innerHTML = '🔒';
      }
    });
  });
}

// Password Strength Meter
function initPasswordStrength() {
  const pwdInput = document.getElementById('signup-password');
  const meterFill = document.getElementById('strength-fill');
  const strengthText = document.getElementById('strength-text');

  if (!pwdInput || !meterFill) return;

  pwdInput.addEventListener('input', () => {
    const val = pwdInput.value;
    let score = 0;

    if (val.length >= 6) score += 25;
    if (val.length >= 10) score += 25;
    if (/[A-Z]/.test(val) && /[a-z]/.test(val)) score += 25;
    if (/[0-9]/.test(val) || /[^A-Za-z0-9]/.test(val)) score += 25;

    meterFill.style.width = `${score}%`;

    if (score <= 25) {
      meterFill.style.backgroundColor = '#E75A5A';
      if (strengthText) strengthText.textContent = 'Weak';
    } else if (score <= 50) {
      meterFill.style.backgroundColor = '#EBB344';
      if (strengthText) strengthText.textContent = 'Fair';
    } else if (score <= 75) {
      meterFill.style.backgroundColor = '#5B9CF6';
      if (strengthText) strengthText.textContent = 'Good';
    } else {
      meterFill.style.backgroundColor = '#4EBA74';
      if (strengthText) strengthText.textContent = 'Strong';
    }
  });
}

// Signup Form Handler
function initSignupForm() {
  const form = document.getElementById('signup-form');
  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const fullName = document.getElementById('full-name').value.trim();
    const email = document.getElementById('email').value.trim();
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('signup-password').value;
    const confirmPassword = document.getElementById('confirm-password').value;
    const terms = document.getElementById('terms').checked;
    const submitBtn = document.getElementById('signup-submit-btn');

    // Validation
    if (!fullName || !email || !username || !password || !confirmPassword) {
      showToast('Please fill in all required fields.', 'error');
      return;
    }

    if (password !== confirmPassword) {
      showToast('Passwords do not match.', 'error');
      return;
    }

    if (password.length < 6) {
      showToast('Password must be at least 6 characters long.', 'error');
      return;
    }

    if (!terms) {
      showToast('You must accept the Terms and Conditions to proceed.', 'error');
      return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = 'CREATING ACCOUNT...';

    try {
      const response = await Api.register({
        fullName,
        email,
        username,
        password,
        confirmPassword
      });

      // Automatically authenticate and save session
      Auth.setSession(response.data, true);
      showToast('Account created successfully! Welcome to the studio.', 'success');

      setTimeout(() => {
        window.location.href = 'dashboard.html';
      }, 1200);

    } catch (err) {
      showToast(err.message, 'error');
      submitBtn.disabled = false;
      submitBtn.textContent = 'CREATE ACCOUNT';
    }
  });
}

// Login Form Handler
function initLoginForm() {
  const form = document.getElementById('login-form');
  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const usernameOrEmail = document.getElementById('login-id').value.trim();
    const password = document.getElementById('login-password').value;
    const rememberMe = document.getElementById('remember-me').checked;
    const submitBtn = document.getElementById('login-submit-btn');

    if (!usernameOrEmail || !password) {
      showToast('Please enter both your identifier and password.', 'error');
      return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = 'AUTHENTICATING...';

    try {
      const response = await Api.login({
        usernameOrEmail,
        password,
        rememberMe
      });

      Auth.setSession(response.data, rememberMe);
      showToast('Authentication successful. Redirecting to your workspace...', 'success');

      setTimeout(() => {
        window.location.href = 'dashboard.html';
      }, 900);

    } catch (err) {
      showToast(err.message, 'error');
      submitBtn.disabled = false;
      submitBtn.textContent = 'ENTER STUDIO';
    }
  });
}

// Forgot Password Flow
function initForgotPasswordForm() {
  const form = document.getElementById('forgot-form');
  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('forgot-email').value.trim();
    const devBox = document.getElementById('dev-token-box');
    const tokenDisplay = document.getElementById('generated-token');
    const submitBtn = document.getElementById('forgot-submit-btn');

    if (!email) {
      showToast('Please enter your registered email address.', 'error');
      return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = 'GENERATING TOKEN...';

    try {
      const response = await Api.forgotPassword(email);
      showToast(response.message, 'info');

      if (devBox && tokenDisplay && response.data?.token) {
        tokenDisplay.textContent = response.data.token;
        devBox.style.display = 'block';
      }

      submitBtn.disabled = false;
      submitBtn.textContent = 'GENERATE RESET TOKEN';
    } catch (err) {
      showToast(err.message, 'error');
      submitBtn.disabled = false;
      submitBtn.textContent = 'GENERATE RESET TOKEN';
    }
  });
}

// Reset Password Execution Flow
function initResetPasswordForm() {
  const form = document.getElementById('reset-form');
  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const token = document.getElementById('reset-token').value.trim();
    const newPassword = document.getElementById('new-password').value;
    const confirmPassword = document.getElementById('confirm-new-password').value;
    const submitBtn = document.getElementById('reset-submit-btn');

    if (!token || !newPassword || !confirmPassword) {
      showToast('Please fill in all fields.', 'error');
      return;
    }

    if (newPassword !== confirmPassword) {
      showToast('Passwords do not match.', 'error');
      return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = 'UPDATING PASSWORD...';

    try {
      await Api.resetPassword({ token, newPassword, confirmPassword });
      showToast('Password reset successfully! Redirecting to login...', 'success');

      setTimeout(() => {
        window.location.href = 'login.html';
      }, 1500);
    } catch (err) {
      showToast(err.message, 'error');
      submitBtn.disabled = false;
      submitBtn.textContent = 'SET NEW PASSWORD';
    }
  });
}
