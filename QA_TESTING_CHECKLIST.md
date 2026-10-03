# 🧪 React Native Authentication - Testing & QA Checklist

## Pre-Testing Setup

```bash
# Install dependencies
cd frontend
npm install

# Type check
npm run type-check

# Should output:
# ✅ No errors in auth screens
# ✅ Only pre-existing errors in other screens

# Lint
npm run lint

# Build for testing
npm run android  # or npm run ios
```

---

## 📱 Screen-by-Screen Testing

### 1. SplashScreen Testing

#### Launch & Animation
- [ ] App starts and shows SplashScreen
- [ ] Fade-in animation plays (800ms)
- [ ] PetCare logo displays with paw icon
- [ ] Hero section shows pet emojis
- [ ] Tagline visible: "Treat your pets with the very best care!"

#### Navigation
- [ ] "Get Started" button navigates to RoleSelectionScreen
- [ ] "Already have account? Login" link navigates to LoginScreen
- [ ] Buttons are touch-responsive
- [ ] No console errors

#### Layout & Styling
- [ ] Hero image container has purple background
- [ ] Button has shadow effect
- [ ] Text colors match design (purple primary color)
- [ ] Responsive on different screen sizes
- [ ] Safe area respected (no cutoff)

---

### 2. RoleSelectionScreen Testing

#### Display
- [ ] Header shows "Who u are"
- [ ] Subtitle shows role explanation text
- [ ] Three role cards display:
  - [ ] Pet Owner (pink #FF6B9D, paw icon 🐾)
  - [ ] Veterinarian (teal #4ECDC4, stethoscope icon ⚕️)
  - [ ] Assistant (yellow #FFD93D, people icon 👥)
- [ ] Each card shows name and description
- [ ] Icons have colored background containers

#### User Interaction
- [ ] Selecting Pet Owner → RegisterScreen with role=PET_OWNER
- [ ] Selecting Veterinarian → RegisterScreen with role=DOCTOR
- [ ] Selecting Assistant → RegisterScreen with role=ASSISTANT
- [ ] Each card has ripple/highlight effect
- [ ] Back button returns to SplashScreen

#### Card Design
- [ ] Card borders match role color
- [ ] Card icons are circular
- [ ] Text is readable
- [ ] Proper spacing between cards
- [ ] Information text at bottom visible

---

### 3. RegisterScreen Testing

#### Common Fields (All Roles)
- [ ] Full Name field:
  - [ ] Accepts text input
  - [ ] Error: Less than 3 characters
  - [ ] Error: More than 100 characters
- [ ] Username field:
  - [ ] Accepts alphanumeric + underscore
  - [ ] Error: Special characters rejected
  - [ ] Error: Spaces rejected
  - [ ] Error: Less than 3 characters
  - [ ] Error: More than 50 characters
- [ ] Email field:
  - [ ] Accepts valid email format
  - [ ] Error: Invalid email format
  - [ ] Keyboard type is email-address
- [ ] Phone Number field:
  - [ ] Accepts phone format
  - [ ] Error: Less than 7 characters
  - [ ] Keyboard type is phone-pad
- [ ] Address field:
  - [ ] Accepts multiline input
  - [ ] Error: Less than 10 characters
  - [ ] Displays multiple lines correctly
- [ ] Date of Birth field:
  - [ ] Date picker opens on tap
  - [ ] Cannot select future dates
  - [ ] Selected date displays in correct format
  - [ ] Closes after selection

#### Password Fields
- [ ] Password field:
  - [ ] Accepts input
  - [ ] Shows as dots/masked
  - [ ] Show/hide toggle works
  - [ ] Error: Less than 8 characters
  - [ ] Error: No uppercase letter
  - [ ] Error: No lowercase letter
  - [ ] Error: No number
  - [ ] Error: No special character (@$!%*?&)
- [ ] Password Strength Indicator:
  - [ ] Weak password (< 3 points) shows red bar
  - [ ] Fair password (3-4 points) shows orange bar
  - [ ] Good password (5-6 points) shows green bar
  - [ ] Text label updates: "Weak" → "Fair" → "Good" → "Strong"
  - [ ] Bar fills proportionally to score
- [ ] Confirm Password field:
  - [ ] Error: Doesn't match password
  - [ ] Success: Matches password
  - [ ] Show/hide toggle works independently

#### Role Selector
- [ ] Dropdown shows three options
- [ ] Can select each role
- [ ] Default is Pet Owner
- [ ] Cannot be empty

#### Doctor-Specific Fields (Only DOCTOR role)
- [ ] License ID field (visible only when role=DOCTOR):
  - [ ] Error: Less than 5 characters
  - [ ] Accepts text input
- [ ] Specialization field (visible only when role=DOCTOR):
  - [ ] Error: Less than 3 characters
  - [ ] Accepts text input
- [ ] Years of Experience field (visible only when role=DOCTOR):
  - [ ] Accepts number input only
  - [ ] Error: Less than 0
  - [ ] Error: More than 70

#### Form Submission
- [ ] Create Account button disabled when:
  - [ ] Any required field is empty
  - [ ] Any validation error exists
  - [ ] Form is submitting
- [ ] Button shows loading spinner during submission
- [ ] Success navigates to LoginScreen
- [ ] Error shows alert with message
- [ ] Already have account link navigates to LoginScreen
- [ ] Back button returns to RoleSelectionScreen

#### Field Validation Messages
- [ ] Error messages appear below field
- [ ] Error color is red (#FF3B30)
- [ ] Messages are clear and helpful
- [ ] Errors clear on valid input

---

### 4. LoginScreen Testing

#### Form Fields
- [ ] Username/Email field:
  - [ ] Accepts text input
  - [ ] Keyboard type is email-address
  - [ ] Error: Less than 3 characters
  - [ ] Error: Empty field
- [ ] Password field:
  - [ ] Accepts input
  - [ ] Shows as masked dots
  - [ ] Show/hide toggle works
  - [ ] Error: Empty field
  - [ ] Error: Less than 6 characters

#### Password Toggle
- [ ] Eye icon visible in password field
- [ ] Clicking toggle shows/hides password
- [ ] Icon changes between 👁️ and 👁️‍🗨️
- [ ] Toggle doesn't interfere with input

#### Navigation Links
- [ ] "Forgot Password?" navigates to ForgotPasswordScreen
- [ ] "Don't have an account? Sign Up" navigates to RegisterScreen

#### Login Functionality
- [ ] Valid credentials:
  - [ ] Shows loading spinner
  - [ ] API called (check network tab)
  - [ ] Navigates to main app
  - [ ] User data displayed correctly
- [ ] Invalid credentials:
  - [ ] First attempt: Shows error message
  - [ ] Second attempt: Shows "4 attempts remaining"
  - [ ] Third attempt: Shows "3 attempts remaining"
  - [ ] Fourth attempt: Shows "2 attempts remaining"
  - [ ] Fifth attempt: Shows "1 attempt remaining"
  - [ ] Sixth attempt (fails): Shows lockout message

#### Account Lockout
- [ ] After 5 failed attempts:
  - [ ] "Account locked. Try again in 300 seconds." appears
  - [ ] Login button disabled
  - [ ] Login button shows "Account Locked"
  - [ ] Input fields remain disabled
- [ ] Lockout timer:
  - [ ] Counts down from 300 seconds
  - [ ] Updates every second
  - [ ] Shows current countdown: "Try again in 250 seconds"
  - [ ] At 0 seconds, account unlocks automatically
  - [ ] Failed attempts counter resets
  - [ ] Fields become enabled again

#### Error Display
- [ ] Error alert appears
- [ ] Alert has red border
- [ ] Error message is readable
- [ ] Close/dismiss button works
- [ ] Error clears on next attempt

#### Loading State
- [ ] Button shows spinner during login
- [ ] Button text changes or hides
- [ ] Fields disabled during submission
- [ ] Button disabled during submission

---

### 5. ForgotPasswordScreen Testing

#### Step 1: Email Verification
- [ ] Header shows "Reset Password"
- [ ] Subtitle says "Enter your email address"
- [ ] Email field accepts input
- [ ] Email validation:
  - [ ] Error: Invalid email format
  - [ ] Error: Empty field
- [ ] Send OTP button:
  - [ ] Shows loading spinner on click
  - [ ] API called with email
  - [ ] On success: Transitions to step 2 (OTP)
  - [ ] On error: Shows error alert

#### Step 2: OTP Verification & Password Reset
- [ ] Header still shows "Reset Password"
- [ ] Subtitle says "Enter the OTP sent to your email"
- [ ] OTP field:
  - [ ] Keyboard is number-pad
  - [ ] Max length is 6 digits
  - [ ] Error: Not exactly 6 digits
  - [ ] Error: Contains letters
- [ ] New Password field:
  - [ ] Shows as masked dots
  - [ ] Show/hide toggle works
  - [ ] Validates same as register password
  - [ ] Password Strength Indicator updates in real-time
  - [ ] Error messages for validation failures
- [ ] Confirm Password field:
  - [ ] Shows as masked dots
  - [ ] Show/hide toggle works
  - [ ] Error: Doesn't match new password
  - [ ] Independent show/hide toggle
- [ ] Reset Password button:
  - [ ] Disabled until form is valid
  - [ ] Shows loading spinner on click
  - [ ] API called with OTP + password
  - [ ] On success: Shows success message
- [ ] Success Flow:
  - [ ] "Password reset successfully!" message appears
  - [ ] 2-second delay
  - [ ] Auto-redirects to LoginScreen

#### Navigation
- [ ] Back button on Step 1:
  - [ ] Returns to LoginScreen
- [ ] Back button on Step 2:
  - [ ] Returns to Step 1 (email entry)
  - [ ] Clears success/error messages
- [ ] Can navigate back and forth between steps

#### Error Handling
- [ ] Invalid OTP:
  - [ ] Shows error message
  - [ ] Stays on Step 2
  - [ ] Doesn't auto-navigate
- [ ] Network error:
  - [ ] Shows error alert
  - [ ] User can retry
- [ ] Validation errors:
  - [ ] Field-level errors appear
  - [ ] Submit button remains disabled

---

## 🎨 UI/UX Testing

### Colors & Styling
- [ ] Primary color (purple #6B4C9A) used consistently
- [ ] Error color (red #FF3B30) for errors
- [ ] Success color (green #34C759) for strength indicators
- [ ] All text readable (contrast ratio WCAG AA)
- [ ] Button colors appropriate
- [ ] Borders and separators visible

### Spacing & Layout
- [ ] No overlapping elements
- [ ] Proper padding around edges
- [ ] Fields have consistent height
- [ ] Buttons are properly sized
- [ ] Text doesn't cut off
- [ ] Responsive on small/large screens

### Typography
- [ ] Headers are bold
- [ ] Body text is readable
- [ ] Error text is small but visible
- [ ] Font sizes appropriate for mobile
- [ ] Line height is comfortable

### Animations
- [ ] SplashScreen fade-in smooth
- [ ] No janky animations
- [ ] Button interactions responsive
- [ ] Loading spinners smooth
- [ ] Transitions between screens smooth

---

## ⌨️ Keyboard & Input Testing

### Keyboard Types
- [ ] Email field shows email keyboard (@ symbol)
- [ ] Phone field shows phone keyboard
- [ ] Number fields show number keyboard
- [ ] Text fields show normal keyboard
- [ ] Keyboard appears/disappears appropriately

### Keyboard Behavior
- [ ] Keyboard doesn't overlap input (iOS)
- [ ] ScrollView scrolls when keyboard open
- [ ] Focus moves to next field logically
- [ ] Return/Done button works
- [ ] Keyboard dismisses on submit
- [ ] No keyboard left after navigation

### Text Input
- [ ] Can type in all fields
- [ ] Backspace works
- [ ] Copy/paste works
- [ ] Max length enforced
- [ ] Special characters handled

---

## 🔐 Security Testing

### Password Security
- [ ] Password not visible as plain text
- [ ] Show/hide toggle works
- [ ] No password logged to console
- [ ] Strength indicator accurate
- [ ] Special character validation working

### Form Validation
- [ ] All validations enforced
- [ ] Errors prevent submission
- [ ] Invalid data not sent to API
- [ ] Trimmed before validation

### Authentication Flow
- [ ] Tokens stored after login
- [ ] Can logout and clear tokens
- [ ] Session expires appropriately
- [ ] Refresh token works
- [ ] No tokens logged

---

## ♿ Accessibility Testing

### Touch Targets
- [ ] All buttons 44+ pixels
- [ ] Spacing between buttons adequate
- [ ] Easy to tap accurately
- [ ] No precision required

### Color Contrast
- [ ] All text readable
- [ ] Error text visible on background
- [ ] Buttons readable
- [ ] No color-only indicators

### Labels & Descriptions
- [ ] All fields have labels
- [ ] Error messages clear
- [ ] Helper text available
- [ ] Screen reader compatible

### Navigation
- [ ] Can navigate with keyboard
- [ ] Focus visible
- [ ] No keyboard traps
- [ ] Logical tab order

---

## 🔄 Navigation Testing

### Screen Navigation
- [ ] Splash → Role Selection (Get Started)
- [ ] Role Selection → Register (with role param)
- [ ] Register → Login (on success or link)
- [ ] Login → Main App (on success)
- [ ] Login → Forgot Password (link)
- [ ] Forgot Password → Login (back or success)

### Back Navigation
- [ ] Back button available where needed
- [ ] Back button returns to previous screen
- [ ] Back button doesn't lose data (in some cases)
- [ ] Android hardware back button works

### Parameter Passing
- [ ] Role passed correctly to RegisterScreen
- [ ] Route params accessible
- [ ] Deep linking possible

---

## 🌐 Network Testing

### API Integration
- [ ] API endpoints called correctly
- [ ] Request format correct
- [ ] Response parsed correctly
- [ ] Tokens sent in headers
- [ ] Error responses handled

### Network Conditions
- [ ] Works on slow network
- [ ] Works on offline (appropriate error)
- [ ] Request timeout handled
- [ ] Retry logic works
- [ ] No duplicate requests

### Loading States
- [ ] Loading spinner shows
- [ ] Fields disabled during request
- [ ] Can't submit twice
- [ ] Appropriate wait messaging

---

## 📊 Data Testing

### Form Data
- [ ] Data captured correctly
- [ ] Formatting correct
- [ ] Date/time converted properly
- [ ] Role stored correctly
- [ ] Password encrypted

### State Management
- [ ] Form values persist on blur
- [ ] Errors persist until fixed
- [ ] Data cleared on logout
- [ ] No stale data

---

## 🐛 Error Scenario Testing

### Invalid Inputs
- [ ] Special characters in username
- [ ] Very long inputs
- [ ] Empty required fields
- [ ] Invalid formats handled
- [ ] Graceful error messages

### Network Errors
- [ ] Server unavailable
- [ ] Network timeout
- [ ] Invalid response format
- [ ] Error recovery possible

### Edge Cases
- [ ] Very fast repeated submissions
- [ ] Navigation during submission
- [ ] Form changes during submission
- [ ] Multiple screens open (if possible)

---

## 📱 Device-Specific Testing

### Screen Sizes
- [ ] Small phone (4-5")
- [ ] Large phone (6-7")
- [ ] Tablet (if applicable)
- [ ] Landscape orientation
- [ ] Notch/safe area handled

### OS Versions
- [ ] Android 8+
- [ ] iOS 13+
- [ ] Latest versions

### Device Types
- [ ] Real device testing
- [ ] Emulator/simulator testing
- [ ] Different manufacturers (Android)

---

## 📝 Final Sign-off Checklist

### Code Quality
- [ ] TypeScript compiles (0 auth errors)
- [ ] Linting passes
- [ ] No console errors
- [ ] No console.log statements
- [ ] Proper error handling

### Functionality
- [ ] All screens working
- [ ] All buttons functional
- [ ] All validations working
- [ ] Navigation working
- [ ] API integration ready

### Design
- [ ] Follows design system
- [ ] Colors correct
- [ ] Spacing consistent
- [ ] Responsive
- [ ] Accessible

### Documentation
- [ ] Code commented where needed
- [ ] Documentation files complete
- [ ] README updated
- [ ] API docs available

### Performance
- [ ] No memory leaks
- [ ] Smooth animations
- [ ] Fast navigation
- [ ] Efficient validations

---

## ✅ Ready for Production?

All of the following must be checked:

- [ ] All unit tests passing
- [ ] All manual QA tests passing
- [ ] TypeScript compilation clean (auth screens)
- [ ] Linting passing
- [ ] No console errors
- [ ] API endpoints ready on backend
- [ ] Error handling complete
- [ ] Performance acceptable
- [ ] Accessibility compliant
- [ ] Documentation complete
- [ ] Security best practices followed
- [ ] Code reviewed
- [ ] UX approved
- [ ] Stakeholders sign off

---

## 📞 Known Issues & Workarounds

| Issue | Status | Workaround |
|-------|--------|-----------|
| Social login UI | ⏳ Not implemented | Available after backend ready |
| Biometric auth | ⏳ Not implemented | Can be added in phase 2 |
| Dark mode | ⏳ Not implemented | Theme system supports it |
| Multi-language | ⏳ Not implemented | Architecture supports i18n |

---

## 📋 Test Results Summary

**Date Tested:** _____________
**Tester Name:** _____________
**Device/OS:** _____________

### Summary
- Total Tests: ___/___
- Passed: ___
- Failed: ___
- Blocked: ___

### Notes
_____________________________________________
_____________________________________________
_____________________________________________

### Sign-off
- [ ] All critical issues resolved
- [ ] Ready for release
- [ ] Ready for user testing

**Approved By:** _________________ **Date:** _______

---

**Remember:** Quality assurance is ongoing. Keep testing as features are developed and integrated!
