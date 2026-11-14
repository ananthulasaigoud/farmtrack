// reports.js
// API used:
//   GET /api/reports/crops                  crop-wise profit and loss
//   GET /api/reports/best-crop              best crop (204 = no sales yet)
//   GET /api/reports/monthly?year=2026      monthly income/expenses
//   GET /api/reports/download?type=...      CSV file

const user = requireLogin();
const yearSelect = document.getElementById('year');

fillYearDropdown(yearSelect);
yearSelect.addEventListener('change', loadMonthly);

loadCropReport();
loadBestCrop();
loadMonthly();

async function loadCropReport() {
  try {
    const crops = await api.get('/reports/crops');
    if (crops.length === 0) {
      document.getElementById('cropTable').innerHTML = '<tr><td colspan="6" class="empty">No crops yet</td></tr>';
      return;
    }

    let totalExpenses = 0;
    let totalIncome = 0;
    let rows = '';
    crops.forEach((c) => {
      totalExpenses += c.totalExpenses;
      totalIncome += c.totalIncome;
      rows += `
        <tr>
          <td><a href="crop-details.html?id=${c.cropId}">${escapeHtml(c.cropName)}</a></td>
          <td>${escapeHtml(c.season) || '-'}</td>
          <td class="right">${formatMoney(c.totalExpenses)}</td>
          <td class="right">${formatMoney(c.totalIncome)}</td>
          <td class="right ${profitClass(c.profit)}"><b>${formatMoney(c.profit)}</b></td>
          <td>${c.status}</td>
        </tr>`;
    });

    const totalProfit = totalIncome - totalExpenses;
    rows += `
      <tr>
        <td colspan="2"><b>Total</b></td>
        <td class="right"><b>${formatMoney(totalExpenses)}</b></td>
        <td class="right"><b>${formatMoney(totalIncome)}</b></td>
        <td class="right ${profitClass(totalProfit)}"><b>${formatMoney(totalProfit)}</b></td>
        <td></td>
      </tr>`;
    document.getElementById('cropTable').innerHTML = rows;
  } catch (error) {
    showMessage('message', error.message);
  }
}

async function loadBestCrop() {
  const box = document.getElementById('bestCrop');
  try {
    const best = await api.get('/reports/best-crop'); // null when the backend returns 204
    if (!best) {
      box.innerHTML = '<p class="empty">No sales recorded yet</p>';
      return;
    }
    box.innerHTML = `
      <div class="trophy">🏆</div>
      <h2>${escapeHtml(best.cropName)}</h2>
      <div class="stat-value profit">${formatMoney(best.profit)}</div>`;
  } catch (error) {
    showMessage('message', error.message);
  }
}

async function loadMonthly() {
  try {
    const months = await api.get('/reports/monthly?year=' + yearSelect.value);
    let totalIncome = 0;
    let totalExpenses = 0;
    let rows = '';
    months.forEach((m) => {
      totalIncome += m.totalIncome;
      totalExpenses += m.totalExpenses;
      rows += `
        <tr>
          <td>${m.monthName} ${m.year}</td>
          <td class="right">${formatMoney(m.totalIncome)}</td>
          <td class="right">${formatMoney(m.totalExpenses)}</td>
          <td class="right ${profitClass(m.profit)}">${formatMoney(m.profit)}</td>
        </tr>`;
    });
    const totalProfit = totalIncome - totalExpenses;
    rows += `
      <tr>
        <td><b>Total ${yearSelect.value}</b></td>
        <td class="right"><b>${formatMoney(totalIncome)}</b></td>
        <td class="right"><b>${formatMoney(totalExpenses)}</b></td>
        <td class="right ${profitClass(totalProfit)}"><b>${formatMoney(totalProfit)}</b></td>
      </tr>`;
    document.getElementById('monthTable').innerHTML = rows;
  } catch (error) {
    showMessage('message', error.message);
  }
}

async function download(type) {
  try {
    await downloadReport(type, yearSelect.value);
  } catch (error) {
    showMessage('message', error.message);
  }
}
