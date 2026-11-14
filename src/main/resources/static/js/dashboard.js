// dashboard.js
// Calls GET /api/dashboard/summary and GET /api/dashboard/monthly and shows the results.

const user = requireLogin();
const yearSelect = document.getElementById('year');

fillYearDropdown(yearSelect);
yearSelect.addEventListener('change', loadMonthlyChart);

loadSummary();
loadMonthlyChart();

async function loadSummary() {
  try {
    const summary = await api.get('/dashboard/summary');

    document.getElementById('totalCrops').textContent = summary.totalCrops;
    document.getElementById('totalExpenses').textContent = formatMoney(summary.totalExpenses);
    document.getElementById('totalIncome').textContent = formatMoney(summary.totalIncome);

    const profitElement = document.getElementById('profit');
    profitElement.textContent = formatMoney(summary.profit);
    profitElement.className = 'stat-value ' + profitClass(summary.profit);
    document.getElementById('profitLabel').textContent = summary.profit < 0 ? 'Total loss' : 'Total profit';

    showCategoryBars(summary.expensesByCategory, summary.totalExpenses);
    showCropTable(summary.crops);
    showBestCrop(summary.bestCrop);
  } catch (error) {
    showMessage('message', error.message);
  }
}

// Expenses by category as horizontal bars (width = share of total expenses)
function showCategoryBars(expensesByCategory, totalExpenses) {
  let html = '';
  for (const category in expensesByCategory) {
    const amount = expensesByCategory[category];
    if (amount > 0) {
      const percent = (amount / totalExpenses) * 100;
      html += `
        <div class="hbar-row">
          <div class="hbar-text"><span>${CATEGORY_LABELS[category]}</span><b>${formatMoney(amount)}</b></div>
          <div class="hbar-track"><div class="hbar-fill" style="width: ${percent}%"></div></div>
        </div>`;
    }
  }
  document.getElementById('categoryBars').innerHTML = html || '<p class="empty">No expenses yet</p>';
}

function showCropTable(crops) {
  if (crops.length === 0) {
    document.getElementById('cropTable').innerHTML =
      '<tr><td colspan="4" class="empty">No crops yet. <a href="crops.html">Add your first crop</a></td></tr>';
    return;
  }

  let rows = '';
  crops.forEach((crop) => {
    rows += `
      <tr>
        <td><a href="crop-details.html?id=${crop.cropId}">${escapeHtml(crop.cropName)}</a></td>
        <td class="right">${formatMoney(crop.totalExpenses)}</td>
        <td class="right">${formatMoney(crop.totalIncome)}</td>
        <td class="right ${profitClass(crop.profit)}"><b>${formatMoney(crop.profit)}</b></td>
      </tr>`;
  });
  document.getElementById('cropTable').innerHTML = rows;
}

function showBestCrop(bestCrop) {
  const box = document.getElementById('bestCrop');
  if (!bestCrop) {
    box.innerHTML = '<p class="empty">No sales recorded yet</p>';
    return;
  }
  box.innerHTML = `
    <div class="trophy">🏆</div>
    <h2>${escapeHtml(bestCrop.cropName)}</h2>
    <div class="stat-value profit">${formatMoney(bestCrop.profit)}</div>
    <p class="small">${formatMoney(bestCrop.totalIncome)} income - ${formatMoney(bestCrop.totalExpenses)} expenses</p>`;
}

// Monthly chart made from plain <div>s. Each bar's height = value / biggest value of the year.
async function loadMonthlyChart() {
  const chart = document.getElementById('monthlyChart');
  try {
    const months = await api.get('/dashboard/monthly?year=' + yearSelect.value);

    let biggest = 0;
    months.forEach((m) => {
      biggest = Math.max(biggest, m.totalIncome, m.totalExpenses);
    });

    if (biggest === 0) {
      chart.innerHTML = '<p class="empty" style="width:100%">No records in ' + yearSelect.value + '</p>';
      return;
    }

    let html = '';
    months.forEach((m) => {
      const incomeHeight = (m.totalIncome / biggest) * 100;
      const expenseHeight = (m.totalExpenses / biggest) * 100;
      html += `
        <div class="bar-group">
          <div class="bars">
            <div class="bar income" style="height: ${incomeHeight}%" title="Income: ${formatMoney(m.totalIncome)}"></div>
            <div class="bar expense" style="height: ${expenseHeight}%" title="Expenses: ${formatMoney(m.totalExpenses)}"></div>
          </div>
          <span class="bar-label">${m.monthName}</span>
        </div>`;
    });
    chart.innerHTML = html;
  } catch (error) {
    showMessage('message', error.message);
  }
}
