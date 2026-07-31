# Fix and Improvements for 1.9.5-p18 - p25

## Dashboard Tab
1. Actionable Data & Recency (The "Who" and "When")
    - Recent Blocked Logs: Users want to immediately see who was just blocked (e.g., "+1 (555) 012-3456 • 10 mins ago") without needing to navigate away to the "Calls" tab. A small "Recent Activity" list right below the hero card would add instant value.

    - Quick Whitelist / Unblock Action: If a legitimate call gets mistakenly blocked, there’s no quick way to inspect or mark it as safe straight from the summary.

2. Context & Visual Hierarchy
    - Timeframe Context for Weekly Chart: The "Weekly Activity" bar chart lists days (M, T, W, T, F, S, S), but doesn't specify which week (e.g., "Jul 21 – Jul 27").

    - Redundant Data Cards: The 4 middle grid cards ("Calls This Week", "SMS This Week", "All Time – Calls", "All Time – SMS") repeat numbers that are virtually identical to the top card and graph. Breaking them down into categories like Spam / Telemarketer / Unknown Numbers would be much more useful than just splitting Calls vs. SMS.

3. Interactivity & Controls
    - Toggle / Status Control: The top right shows an • Active status pill, but it looks static. A quick toggle switch right on the dashboard to pause or resume blocking temporarily (e.g., "Pause for 1 hour") is a common utility for blocker apps.

    - Filter/Legend on Graph: The bar chart aggregates data, but it's unclear if the green bars represent Calls only, SMS only, or both combined. A simple legend or toggle on the chart would clarify this.

4. Search & Quick Actions
    - Lookup / Manual Block Bar: A quick search bar or Floating Action Button (FAB) allowing users to manually check a phone number or block a custom prefix/number directly from the main screen.


## Calls Tab
1. Missing UX & Management Features
    - Search / Filter Bar: As the whitelist or blocked list grows, finding a specific number or contact manually in a long list will become tedious without a search bar.

    - Batch / Multi-select Actions: There's only a single delete trash icon per item. No multi-select option to clear or remove multiple numbers at once.

    - Edit / Detail Option: Tapping an entry only gives a trash icon to delete. There's no way to edit the contact name, view notes, or adjust specific rules for that entry.

    - Add from Contacts / Recent Calls: The + button likely lets you type a number, but offering explicit shortcuts like "Add from Phonebook" or "Add from Recent Blocked" saves a lot of manual typing.

2. Layout & Visual Polish
    - Empty Space / Layout Scaling: The card takes up almost the entire screen height while displaying only two small entries. A list layout without the heavy outer card container (or letting the list flow naturally) would scale better for larger lists.

    - Safety Confirmation for Deleting: Make sure tapping that trash icon prompts a quick undo toast or confirmation dialog so users don't accidentally remove a whitelisted shortcode like 123 or 185.

3. Ambiguous Action Icons & UX
    - Confusing Right Action Icon (+👤): The icon on the far right of each item looks like "Add Contact" or "Add to Whitelist". Because it’s not explicitly labeled, users might be unsure whether tapping it will whitelist the number, save it to phone contacts, or unblock it. A clearer icon or quick swipe gesture with explicit labels would avoid accidental actions.
    
    - Unclear Top Right Icons (Download & Filter):The download icon ($\downarrow$) likely means "Export Logs", but on a call log screen, users might mistake it for "Fetch latest logs" or "Backup".
    
    - The filter icon (lines) lacks indicators showing active filter states or sorting parameters (e.g., sort by date vs. frequency).
    
4. Missing Key Details per Log
    - Call Frequency / Attempts Badge: If the same spam number calls 5 times, does it show as 5 separate rows or a stacked list with a counter like +62855... (5)? Stacked logs with call count badges keep the list clean and informative.
    
    - Spam Label / Caller ID Context: Right now, it only shows raw phone numbers. Integrating a local database or crowd-sourced spam lookup (e.g., "Suspected Telemarketer", "Financial Services") would instantly give users context on why it was blocked without having to search the number elsewhere.

5. Missing Quick Actions on Item Tap / Selection
    - No Quick Detail Modal or Tap Response: Tapping a phone number row should ideally open a bottom sheet or action menu offering key utilities:Whitelist / Allow future callsCopy NumberCall Back (if it was a false positive)Report / Tag Spam Category
    
    - Batch Operations / Clear History: There's no quick way to "Clear All Logs" or select multiple entries to whitelist or delete in bulk.

6. Layout & Scroll Design
    - Header Redundancy: The outer section header says Blocked at the top green pill, and then inside the card container it says Blocked again right next to the export icon. Eliminating the duplicate title inside the card frees up vertical space.


## SMS Tab
1. Missing SMS Preview / Snippets
    - No Content Context: Unlike phone calls—where only the number and timestamp matter—SMS messages carry content. When a message is blocked, users need to see a snippet preview (e.g., "Promo diskon 50% untuk..." or OTP codes) directly in the blocked feed. Without a snippet, users can't tell if an important OTP or delivery notification got accidentally caught in the filter without tapping into each log.

2. Keyword & Sender Alpha-ID Filtering
    - No Keyword Filtering: SMS spam is often filtered by text content (e.g., blocking terms like "Pinjaman", "Hadiah", "Judol", or links). The current layout only handles full numbers or shortcodes (like 123 or 185), missing a critical feature for message blocking.

    - Alpha Sender IDs: Many SMS promo messages come from alphanumeric IDs (e.g., TELKOMSEL, BANK_ABC) rather than numerical phone numbers. The input screen for adding to Whitelist/Block list needs to explicitly support text-based sender IDs.

3. Redundant Navigation Structure
    - Calls vs. SMS Separation: Having two almost completely identical screens (Calls and SMS) with identical UI patterns, sub-tabs (Whitelist/Blocked), and toggles feels repetitive. Combining them into a unified "Rules / Logs" tab with simple filter chips (All, Calls, SMS) would clean up the bottom navigation bar and reduce screen duplication.

4. Empty State UX (Blocked Tab)
    - Static Empty State: The shield icon with "No blocked SMS yet" is clean, but it's a missed opportunity to build user trust.

    - Value Reinforcement: Adding a small, reassuring message like "We'll display blocked messages here once detected" or a quick button to "Test Filter" / "View Spam Rules" gives the empty screen actual utility.

5. Whitelist Syncing / Shared Rules
    - Duplicate Whitelists: In the Whitelist tab, shortcodes 123 (PLN) and 185 (Indosat) are shown. If a user whitelists a contact for Calls, they almost certainly want them whitelisted for SMS too. If these lists aren't synced automatically behind the scenes, managing two separate whitelists creates unnecessary friction.