package com.farmtrack.farmtrack.config;

import com.farmtrack.farmtrack.entity.*;
import com.farmtrack.farmtrack.repository.CropRepository;
import com.farmtrack.farmtrack.repository.FarmerRepository;
import com.farmtrack.farmtrack.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Runs once when the application starts.
 * Creates the guest login (if missing) and fills it with sample crops, expenses and sales,
 * so visitors can try the app without registering.
 * With app.guest.reset-on-startup=true the sample data is restored on every restart.
 */
@Component
public class GuestAccountInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(GuestAccountInitializer.class);

    private final GuestAccount guestAccount;
    private final FarmerRepository farmerRepository;
    private final UserRepository userRepository;
    private final CropRepository cropRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean resetOnStartup;

    public GuestAccountInitializer(GuestAccount guestAccount, FarmerRepository farmerRepository,
                                   UserRepository userRepository, CropRepository cropRepository,
                                   PasswordEncoder passwordEncoder,
                                   @Value("${app.guest.reset-on-startup:true}") boolean resetOnStartup) {
        this.guestAccount = guestAccount;
        this.farmerRepository = farmerRepository;
        this.userRepository = userRepository;
        this.cropRepository = cropRepository;
        this.passwordEncoder = passwordEncoder;
        this.resetOnStartup = resetOnStartup;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!guestAccount.isEnabled()) {
            return;
        }

        User guest = userRepository.findByFarmerPhone(guestAccount.getPhone()).orElse(null);
        boolean created = false;
        if (guest == null) {
            guest = createGuestLogin();
            created = true;
        }
        // Make sure the guest password always matches the configured one
        guest.setPasswordHash(passwordEncoder.encode(guestAccount.getPassword()));

        Farmer farmer = guest.getFarmer();
        List<Crop> existingCrops = cropRepository.findByFarmerIdOrderByIdDesc(farmer.getId());
        if (created || existingCrops.isEmpty() || resetOnStartup) {
            cropRepository.deleteAll(existingCrops);
            cropRepository.flush();
            addSampleData(farmer);
            log.info("Guest account ready: phone {} (sample data loaded)", guestAccount.getPhone());
        }
    }

    private User createGuestLogin() {
        Farmer farmer = farmerRepository.findFirstByPhoneOrderByIdAsc(guestAccount.getPhone())
                .orElseGet(() -> farmerRepository.save(
                        new Farmer("Guest Farmer", guestAccount.getPhone(), "Demo Village")));

        User user = new User();
        user.setRole(Role.FARMER);
        user.setFarmer(farmer);
        user.setPasswordHash(passwordEncoder.encode(guestAccount.getPassword()));
        return userRepository.save(user);
    }

    /** Sample data based on the project example: rice gives ₹28,500 profit. Dates are relative to today. */
    private void addSampleData(Farmer farmer) {
        LocalDate today = LocalDate.now();

        Crop rice = newCrop(farmer, "Rice", "Kharif", "2 acres near canal",
                today.minusMonths(5), today.minusDays(15), "BPT 5204 variety");
        addExpense(rice, ExpenseCategory.SEEDS, "2000", today.minusMonths(5), "Paddy seed");
        addExpense(rice, ExpenseCategory.FERTILIZER, "3000", today.minusMonths(4), "Urea and DAP");
        addExpense(rice, ExpenseCategory.PESTICIDE, "1500", today.minusMonths(3), "Pest spray");
        addExpense(rice, ExpenseCategory.LABOUR, "5000", today.minusMonths(1), "Harvest labour");
        addIncome(rice, "20", "quintal", "2000", today.minusDays(10), "Sold at market yard");
        cropRepository.save(rice); // cascade saves the expenses and incomes too

        Crop cotton = newCrop(farmer, "Cotton", "Kharif", "1 acre, black soil",
                today.minusMonths(6), today.minusDays(20), null);
        addExpense(cotton, ExpenseCategory.SEEDS, "1500", today.minusMonths(6), "BT cotton seed");
        addExpense(cotton, ExpenseCategory.FERTILIZER, "2500", today.minusMonths(5), null);
        addExpense(cotton, ExpenseCategory.IRRIGATION, "1000", today.minusMonths(3), "Borewell electricity");
        addExpense(cotton, ExpenseCategory.LABOUR, "4000", today.minusMonths(1), "Cotton picking");
        addIncome(cotton, "5", "quintal", "6000", today.minusDays(5), "Sold to ginning mill");
        cropRepository.save(cotton);

        Crop maize = newCrop(farmer, "Maize", "Rabi", "1 acre",
                today.minusDays(20), today.plusMonths(3), "Recently sown, no sale yet");
        addExpense(maize, ExpenseCategory.SEEDS, "1200", today.minusDays(20), "Hybrid maize seed");
        cropRepository.save(maize);
    }

    private Crop newCrop(Farmer farmer, String name, String season, String land,
                         LocalDate sowing, LocalDate harvest, String notes) {
        Crop crop = new Crop();
        crop.setFarmer(farmer);
        crop.setCropName(name);
        crop.setSeason(season);
        crop.setLandDetails(land);
        crop.setSowingDate(sowing);
        crop.setExpectedHarvestDate(harvest);
        crop.setAdditionalNotes(notes);
        return crop;
    }

    private void addExpense(Crop crop, ExpenseCategory category, String amount, LocalDate date, String description) {
        Expense expense = new Expense();
        expense.setCrop(crop);
        expense.setCategory(category);
        expense.setAmount(new BigDecimal(amount));
        expense.setExpenseDate(date);
        expense.setDescription(description);
        crop.getExpenses().add(expense);
    }

    private void addIncome(Crop crop, String quantity, String unit, String price, LocalDate date, String description) {
        Income income = new Income();
        income.setCrop(crop);
        income.setQuantity(new BigDecimal(quantity));
        income.setUnit(unit);
        income.setPricePerUnit(new BigDecimal(price));
        income.setTotalAmount(income.getQuantity().multiply(income.getPricePerUnit()).setScale(2, RoundingMode.HALF_UP));
        income.setSaleDate(date);
        income.setDescription(description);
        crop.getIncomes().add(income);
    }
}
