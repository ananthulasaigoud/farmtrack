// crop-details.js - one crop with its expenses and sales.
// The crop id comes from the URL: crop-details.html?id=5

const user = requireLogin();
const cropId = getQueryParam('id');

// Buttons
document.getElementById('editCropBtn').addEventListener('click', () => {
  window.location.href = 'crops.html?edit=' + cropId;
});
document.getElementById('deleteCropBtn').addEventListener('click', deleteCrop);

document.getElementById('showExpenseFormBtn').addEventListener('click', () => {
  document.getElementById('expenseForm').reset();
  document.getElementById('expenseDate').value = today();
  hideMessage('expenseFormMessage');
  document.getElementById('expenseForm').hidden = false;
});

document.getElementById('showSaleFormBtn').addEventListener('click', () => {
  document.getElementById('saleForm').reset();
  document.getElementById('saleDate').value = today();
  document.getElementById('saleTotal').textContent = formatMoney(0);
  hideMessage('saleFormMessage');
  document.getElementById('saleForm').hidden = false;
});

document.getElementById('expenseForm').addEventListener('submit', saveExpense);
document.getElementById('saleForm').addEventListener('submit', saveSale);

// Show total = quantity x price while the user types
document.getElementById('quantity').addEventListener('input', updateSaleTotal);
document.getElementById('pricePerUnit').addEventListener('input', updateSaleTotal);

loadPage();

async function loadPage() {
  try {
    // Four API calls at the same time
    const [crop, summary, expenses, sales] = await Promise.all([
      api.get('/crops/' + cropId),
      api.get('/dashboard/crops/' + cropId),
      api.get('/crops/' + cropId + '/expenses'),
      api.get('/crops/' + cropId + '/incomes'),
    ]);
    showCrop(crop);
    showSummary(summary);
    showExpenses(expenses);
    showSales(sales);
  } catch (error) {
    showMessage('message', error.message); // e.g. "Crop not found with id 99"
  }
}

function showCrop(crop) {
  document.title = crop.cropName + ' - FarmTrack';
  document.getElementById('cropName').textContent = crop.cropName;
  document.getElementById('cropSubtitle').textContent =
    (crop.season || 'No season') + ' · Sown ' + formatDate(crop.sowingDate) +
    ' · Harvest ' + formatDate(crop.expectedHarvestDate);
  document.getElementById('landDetails').textContent = crop.landDetails || '-';
  document.getElementById('notes').textContent = crop.additionalNotes || '-';
}

function showSummary(summary) {
  document.getElementById('totalExpenses').textContent = formatMoney(summary.totalExpenses);
  document.getElementById('totalIncome').textContent = formatMoney(summary.totalIncome);

  const profitElement = document.getElementById('profit');
  profitElement.textContent = formatMoney(summary.profit);
  profitElement.className = 'stat-value ' + profitClass(summary.profit);
  document.getElementById('profitLabel').textContent = summary.profit < 0 ? 'Loss' : 'Profit';

  let html = '';
  for (const category in summary.expensesByCategory) {
    const amount = summary.expensesByCategory[category];
    if (amount > 0) {
      const percent = (amount / summary.totalExpenses) * 100;
      html += `
        <div class="hbar-row">
          <div class="hbar-text"><span>${CATEGORY_LABELS[category]}</span><b>${formatMoney(amount)}</b></div>
          <div class="hbar-track"><div class="hbar-fill" style="width: ${percent}%"></div></div>
        </div>`;
    }
  }
  document.getElementById('categoryBars').innerHTML = html || '<p class="empty">No expenses yet</p>';
}

function showExpenses(expenses) {
  if (expenses.length === 0) {
    document.getElementById('expenseTable').innerHTML = '<tr><td colspan="5" class="empty">No expenses yet</td></tr>';
    return;
  }
  let rows = '';
  expenses.forEach((e) => {
    rows += `
      <tr>
        <td>${formatDate(e.expenseDate)}</td>
        <td>${CATEGORY_LABELS[e.category]}</td>
        <td class="small">${escapeHtml(e.description) || '-'}</td>
        <td class="right"><b>${formatMoney(e.amount)}</b></td>
        <td class="actions"><button class="btn btn-danger btn-small" onclick="deleteExpense(${e.id})">Delete</button></td>
      </tr>`;
  });
  document.getElementById('expenseTable').innerHTML = rows;
}

function showSales(sales) {
  if (sales.length === 0) {
    document.getElementById('saleTable').innerHTML = '<tr><td colspan="6" class="empty">No sales yet</td></tr>';
    return;
  }
  let rows = '';
  sales.forEach((s) => {
    rows += `
      <tr>
        <td>${formatDate(s.saleDate)}</td>
        <td>${s.quantity} ${escapeHtml(s.unit)}</td>
        <td class="right">${formatMoney(s.pricePerUnit)}</td>
        <td class="right profit"><b>${formatMoney(s.totalAmount)}</b></td>
        <td class="small">${escapeHtml(s.description) || '-'}</td>
        <td class="actions"><button class="btn btn-danger btn-small" onclick="deleteSale(${s.id})">Delete</button></td>
      </tr>`;
  });
  document.getElementById('saleTable').innerHTML = rows;
}

function updateSaleTotal() {
  const quantity = Number(document.getElementById('quantity').value) || 0;
  const price = Number(document.getElementById('pricePerUnit').value) || 0;
  document.getElementById('saleTotal').textContent = formatMoney(quantity * price);
}

async function saveExpense(event) {
  event.preventDefault();
  const body = {
    category: document.getElementById('category').value,
    amount: Number(document.getElementById('amount').value),
    expenseDate: document.getElementById('expenseDate').value,
    description: document.getElementById('expenseDescription').value.trim() || null,
  };
  try {
    await api.post('/crops/' + cropId + '/expenses', body);
    document.getElementById('expenseForm').hidden = true;
    loadPage();
  } catch (error) {
    showMessage('expenseFormMessage', error.message);
  }
}

async function saveSale(event) {
  event.preventDefault();
  const body = {
    quantity: Number(document.getElementById('quantity').value),
    unit: document.getElementById('unit').value,
    pricePerUnit: Number(document.getElementById('pricePerUnit').value),
    saleDate: document.getElementById('saleDate').value,
    description: document.getElementById('saleDescription').value.trim() || null,
  };
  try {
    await api.post('/crops/' + cropId + '/incomes', body);
    document.getElementById('saleForm').hidden = true;
    loadPage();
  } catch (error) {
    showMessage('saleFormMessage', error.message);
  }
}

async function deleteExpense(id) {
  if (!confirm('Delete this expense?')) return;
  try {
    await api.delete('/expenses/' + id);
    loadPage();
  } catch (error) {
    showMessage('message', error.message);
  }
}

async function deleteSale(id) {
  if (!confirm('Delete this sale?')) return;
  try {
    await api.delete('/incomes/' + id);
    loadPage();
  } catch (error) {
    showMessage('message', error.message);
  }
}

async function deleteCrop() {
  if (!confirm('Delete this crop with all its expenses and sales?')) return;
  try {
    await api.delete('/crops/' + cropId);
    window.location.href = 'crops.html';
  } catch (error) {
    showMessage('message', error.message);
  }
}
