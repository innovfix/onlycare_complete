import './bootstrap';
import Alpine from 'alpinejs';
import Chart from 'chart.js/auto';

window.Alpine = Alpine;
window.Chart = Chart;

Alpine.start();

// Dark mode chart defaults
Chart.defaults.color = '#F1F5F9';
Chart.defaults.borderColor = '#334155';
Chart.defaults.backgroundColor = 'rgba(59, 130, 246, 0.1)';

// Utility functions
window.formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-IN', {
        style: 'currency',
        currency: 'INR',
        minimumFractionDigits: 0
    }).format(amount);
};

window.formatNumber = (number) => {
    return new Intl.NumberFormat('en-IN').format(number);
};

window.formatDuration = (seconds) => {
    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${minutes}m ${secs}s`;
};

// Toast notification
window.showToast = (message, type = 'success') => {
    const toast = document.createElement('div');
    toast.className = `fixed top-4 right-4 px-6 py-3 rounded-lg shadow-lg z-50 alert alert-${type}`;
    toast.textContent = message;
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.remove();
    }, 3000);
};

// Confirm dialog
window.confirmAction = (message) => {
    return confirm(message);
};

