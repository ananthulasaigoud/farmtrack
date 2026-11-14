// income.js - sales history across all crops, with add / edit / delete
// API used:
//   GET    /api/incomes                     my sales history
//   POST   /api/crops/{cropId}/incomes      record a sale
//   PUT    /api/incomes/{id}                update a sale
//   DELETE /api/incomes/{id}                delete a sale
// The backend calculates totalAmount = quantity x pricePerUnit.

const user = requireLogin();

let sales = [];
let editingSaleId = null;

document.getElementById('addSaleBtn').addEventListener('click', () => openForm(null));
document.getElementById('cancelBtn').addEventListener('click', closeForm);
document.getElementById('saleForm').addEventListener('submit', saveSale);
document.getElementById('cropFilter').addEventListener('change', showSales);
document.getElementById('quantity').addEventListener('input', updateTotal);
document.getElementById('pricePerUnit').addEventListener('input', updateTotal);

loadCrops();
loadSales();

async function loadCrops() {
  try {
    const crops = await api.get('/farmers/' + user.farmerId + '/crops');
    fillCropDropdown(document.getElementById('cropId'), crops);
    fillCropDropdown(document.getElementById('cropFilter'), crops, 'All crops');

    if (crops.length === 0) {
      document.getElementById('addSaleBtn').disabled = true;
      showMessage('message', 'Add a crop first. Sales are recorded against a crop.');
    }
  } catch (error) {
    showMessage('message', error.message);
  }
}

async function loadSales() {
  try {
    sales = await api.get('/incomes');
    showSales();
  } catch (error) {
    showMessage('message', error.message);
  }
}

function showSales() {
  const cropFilter = document.getElementById('cropFilter').value;
  const visible = cropFilter ? sales.filter((s) => String(s.cropId) === cropFilter) : sales;

  let total = 0;
  let rows = '';
  visible.forEach((s) => {
    total += s.totalAmount;
    rows += `
      <tr>
        <td>${formatDate(s.saleDate)}</td>
        <td><a href="crop-details.html?id=${s.cropId}">${escapeHtml(s.cropName)}</a></td>
        <td>${s.quantity} ${escapeHtml(s.unit)}<div class="small">${escapeHtml(s.description)}</div></td>
        <td class="right">${formatMoney(s.pricePerUnit)}</td>
        <td class="right profit"><b>${formatMoney(s.totalAmount)}</b></td>
        <td class="actions">
          <button class="btn btn-light btn-small" onclick="editSale(${s.id})">Edit</button>
          <button class="btn btn-danger btn-small" onclick="deleteSale(${s.id})">Delete</button>
        </td>
      </tr>`;
  });

  document.getElementById('saleTable').innerHTML =
    rows || '<tr><td colspan="6" class="empty">No sales recorded</td></tr>';
  document.getElementById('total').textContent = formatMoney(total);
}

function updateTotal() {
  const quantity = Number(document.getElementById('quantity').value) || 0;
  const price = Number(document.getElementById('pricePerUnit').value) || 0;
  document.getElementById('saleTotal').textContent = formatMoney(quantity * price);
}

function openForm(sale) {
  editingSaleId = sale ? sale.id : null;
  document.getElementById('formTitle').textContent = sale ? 'Edit sale' : 'Record sale';

  const cropSelect = document.getElementById('cropId');
  if (sale) cropSelect.value = sale.cropId;
  cropSelect.disabled = Boolean(sale);

  document.getElementById('saleDate').value = sale ? sale.saleDate : today();
  document.getElementById('quantity').value = sale ? sale.quantity : '';
  document.getElementById('unit').value = sale ? sale.unit || 'quintal' : 'quintal';
  document.getElementById('pricePerUnit').value = sale ? sale.pricePerUnit : '';
  document.getElementById('description').value = sale ? sale.description || '' : '';
  updateTotal();

  hideMessage('formMessage');
  document.getElementById('formCard').hidden = false;
  document.getElementById('quantity').focus();
}

function closeForm() {
  document.getElementById('formCard').hidden = true;
  editingSaleId = null;
}

function editSale(id) {
  openForm(sales.find((s) => s.id === id));
}

async function saveSale(event) {
  event.preventDefault();

  const body = {
    quantity: Number(document.getElementById('quantity').value),
    unit: document.getElementById('unit').value,
    pricePerUnit: Number(document.getElementById('pricePerUnit').value),
    saleDate: document.getElementById('saleDate').value,
    description: document.getElementById('description').value.trim() || null,
  };

  try {
    if (editingSaleId) {
      await api.put('/incomes/' + editingSaleId, body);
      showMessage('message', 'Sale updated', 'success');
    } else {
      const cropId = document.getElementById('cropId').value;
      await api.post('/crops/' + cropId + '/incomes', body);
      showMessage('message', 'Sale recorded', 'success');
    }
    closeForm();
    loadSales();
  } catch (error) {
    showMessage('formMessage', error.message);
  }
}

async function deleteSale(id) {
  if (!confirm('Delete this sale?')) return;
  try {
    await api.delete('/incomes/' + id);
    showMessage('message', 'Sale deleted', 'success');
    loadSales();
  } catch (error) {
    showMessage('message', error.message);
  }
}
