# Testing the New Features

## Changes Made

### 1. New App Icon
- Created a modern launcher icon with focus/lock theme
- Features purple gradient background with lock icon and focus rings
- Both regular and round icons updated

### 2. Background Gradients
- Added multiple background gradients for better visual appeal:
  - Main header gradient (dark purple theme)
  - Card backgrounds with subtle gradients
  - Blocked screen gradient

### 3. Coin System Implementation

#### How It Works:
- **Earning Coins**: For every 45 minutes of focused work, you earn 1 coin
- **Spending Coins**: You can spend 500 coins to break a focus session and unlock blocked apps
- **Coin Tracking**: Coins are displayed in the header badge (🪙 icon)

#### Testing the Coin System:

##### Test 1: Earn Coins
1. Start a focus session for 45+ minutes
2. Let the session complete naturally
3. Check the notification - it should show coins earned
4. Open the app and verify the coin badge in the header shows the new balance

##### Test 2: Accumulation
- Coins accumulate across multiple sessions
- If you do a 30-minute session followed by a 15-minute session, you'll earn 1 coin (30+15=45)
- The system tracks partial minutes for the next coin

##### Test 3: Spend Coins to Break Focus
1. Make sure you have at least 500 coins (for testing, you can manually add coins via the app data)
2. Start a focus session and select apps to block
3. Try to open a blocked app
4. On the blocked screen, you'll see:
   - Your current coin balance
   - A button to "Use 500 Coins to Break Focus"
5. Click the button and confirm
6. The session should end and apps should be unlocked
7. Your coin balance should decrease by 500

##### Test 4: Insufficient Coins
1. Have less than 500 coins
2. Start a focus session
3. Try to open a blocked app
4. The coin button should show "Need X more coins" and be disabled

## UI Changes Summary

### Header
- Added coin badge (🪙) next to streak badge
- Applied gradient background

### Main Screen
- Cards now use gradient backgrounds instead of flat colors
- More visually appealing and modern look

### Blocked Screen
- New gradient background
- Coin balance display
- "Use 500 Coins" button (enabled only when balance >= 500)

## Files Modified

### Java Files:
- `SessionManager.java` - Added coin tracking methods
- `MainActivity.java` - Added coin badge UI
- `BlockedActivity.java` - Added coin spending UI and logic
- `AppMonitorService.java` - Added coin awarding on session completion

### Resources:
- `ic_launcher.xml` - New app icon
- `ic_launcher_round.xml` - New round app icon
- `bg_gradient_main.xml` - Main gradient background
- `bg_card.xml` - Card gradient background
- `bg_blocked.xml` - Blocked screen gradient
- `activity_main.xml` - Updated to use gradients and coin badge
- `activity_blocked.xml` - Updated to show coins and coin spending button

## Expected Behavior

1. **Icon**: The app launcher icon should show a purple-themed lock with focus rings
2. **Coins Display**: Coins are always visible in the header as "🪙 X"
3. **Earning**: Complete 45-minute session = +1 coin notification
4. **Spending**: 500 coins allows one-time break of focus session
5. **UI**: Smoother gradients throughout the app for better aesthetics
