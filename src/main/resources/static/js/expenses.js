// expenses.js - expense history across all crops, with add / edit / delete
// API used:
//   GET    /api/expenses?category=SEEDS        my expense history (category optional)
//   POST   /api/crops/{cropId}/expenses        add expense to a crop
//   PUT    /api/expenses/{id}                  update expense
//   DELETE /api/expenses/{id}                  delete expense

const user = requireLogin();

let expenses = [];
let editingExpenseId = null; // null = adding, number = editing

document.getElementById('addExpenseBtn').addEventListener('click', () => openForm(null));
document.getElementById('cancelBtn').addEventListener('click', closeForm);
document.getElementById('expenseForm').addEventListener('submit', saveExpense);
document.getElementById('categoryFilter').addEventListener('change', loadExpenses);
document.getElementById('cropFilter').addEventListener('change', showExpenses);

loadCrops();
loadExpenses();

// The crop dropdowns need the farmer's crops
async function loadCrops() {
  try {
    const crops = await api.get('/farmers/' + user.farmerId + '/crops');
    fillCropDropdown(document.getElementById('cropId'), crops);
    fillCropDropdown(document.getElementById('cropFilter'), crops, 'All crops');

    if (crops.length === 0) {
      document.getElementById('addExpenseBtn').disabled = true;
      showMessage('message', 'Add a crop first. Expenses are recorded against a crop.');
    }
  } catch (error) {
    showMessage('message', error.message);
  }
}

// Category filter is done by the backend (?category=...)
async function loadExpenses() {
  try {
    const category = document.getElementById('categoryFilter').value;
    expenses = await api.get('/expenses' + (category ? '?category=' + category : ''));
    showExpenses();
  } catch (error) {
    showMessage('message', error.message);
  }
}

// Crop filter is done here in JavaScript
function showExpenses() {
  const cropFilter = document.getElementById('cropFilter').value;
  const visible = cropFilter ? expenses.filter((e) => String(e.cropId) === cropFilter) : expenses;

  let total = 0;
  let rows = '';
  visible.forEach((e) => {
    total += e.amount;
    rows += `
      <tr>
        <td>${formatDate(e.expenseDate)}</td>
        <td><a href="crop-details.html?id=${e.cropId}">${escapeHtml(e.cropName)}</a></td>
        <td>${CATEGORY_LABELS[e.category]}</td>
        <td class="small">${escapeHtml(e.description) || '-'}</td>
        <td class="right"><b>${formatMoney(e.amount)}</b></td>
        <td class="actions">
          <button class="btn btn-light btn-small" onclick="editExpense(${e.id})">Edit</button>
          <button class="btn btn-danger btn-small" onclick="deleteExpense(${e.id})">Delete</button>
        </td>
      </tr>`;
  });

  document.getElementById('expenseTable').innerHTML =
    rows || '<tr><td colspan="6" class="empty">No expenses found</td></tr>';
  document.getElementById('total').textContent = formatMoney(total);
}

function openForm(expense) {
  editingExpenseId = expense ? expense.id : null;
  document.getElementById('formTitle').textContent = expense ? 'Edit expense' : 'Add expense';

  const cropSelect = document.getElementById('cropId');
  if (expense) cropSelect.value = expense.cropId;
  cropSelect.disabled = Boolean(expense); // an expense stays on its crop when edited

  document.getElementById('category').value = expense ? expense.category : 'SEEDS';
  document.getElementById('amount').value = expense ? expense.amount : '';
  document.getElementById('expenseDate').value = expense ? expense.expenseDate : today();
  document.getElementById('description').value = expense ? expense.description || '' : '';

  hideMessage('formMessage');
  document.getElementById('formCard').hidden = false;
  document.getElementById('amount').focus();
}

function closeForm() {
  document.getElementById('formCard').hidden = true;
  editingExpenseId = null;
}

function editExpense(id) {
  openForm(expenses.find((e) => e.id === id));
}

async function saveExpense(event) {
  event.preventDefault();

  const body = {
    category: document.getElementById('category').value,
    amount: Number(document.getElementById('amount').value),
    expenseDate: document.getElementById('expenseDate').value,
    description: document.getElementById('description').value.trim() || null,
  };

  try {
    if (editingExpenseId) {
      await api.put('/expenses/' + editingExpenseId, body);
      showMessage('message', 'Expense updated', 'success');
    } else {
      const cropId = document.getElementById('cropId').value;
      await api.post('/crops/' + cropId + '/expenses', body);
      showMessage('message', 'Expense added', 'success');
    }
    closeForm();
    loadExpenses();
  } catch (error) {
    showMessage('formMessage', error.message);
  }
}

async function deleteExpense(id) {
  if (!confirm('Delete this expense?')) return;
  try {
    await api.delete('/expenses/' + id);
    showMessage('message', 'Expense deleted', 'success');
    loadExpenses();
  } catch (error) {
    showMessage('message', error.message);
  }
}
