# Scan-ing

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
This app will be used to scan products (barcode or ingredients list) for harmful chemicals or ingredients. Also, whether the user has allergies or even if the user would not like to consume a product with a certain additive, Scan-ing will help warn the user when they are about to consume a product that is harmful.



### App Evaluation
[Evaluation of your app across the following attributes]
- **Category:** Health
- **Mobile:** 
- **Story:** 
- **Market:** For anyone who would like to increase their awareness of what they are injesting 
- **Habit:** 
- **Scope:**

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**


1. The user can scan the ingredients list or the barcode of the product

    - Send request to open food API to check if there are any matches for additives/preservatives

2. User can add or remove allergies or ingredients they would not like to have
    
    
3. Send request to open food API to suggest alternative products if the User's prouct is harmful


4. Find the suggested items in nearby grocery stores using some API (Walmart, walgreens, etc)


5. The user can add any of the suggested items to their shopping list/cart





**Optional Nice-to-have Stories**

 1. The user gets notified to pickup items in their shopping cart while in a grocery store location (possibly a stretch feature)
 

### 2. Screen Archetypes

* "home" Screen where user can have access to scan a product from here or navigate to any other screen, also shows list of recent scans
   * This is linked to user story #1
   * ...
* user "profile" screen where they can adjust their preferences (add or remove allergies, or ingredients they wouldn't like to have)
   * This is linked to user story #2
   * ...

* Screen with nearby grocery stores and shows if the stock in each store includes an item from the user's shopping cart
    * This is linked to user story #4


* "shopping cart/list" for products the user adds themselves, or products that were recommended to the user and they selected the option to add it to their cart
    * This is linked to user story #5

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Home - navigates to home screen
* Profile - navigates to profile
* Shopping cart - navigates to shopping cart screen

**Flow Navigation** (Screen to Screen)

* Home
   * Only screen where we can "navigate" to or access the product scanner
   * can also navigate to any other screen from here with a bottom navigation bar
* Profile
   * Can navigate to the home screen or navigate to the shopping cart screen from here
   * ...
* Shopping cart 
    * Can navigate to the home screen or navigate to the profile screen from here
    * Only screen where we can navigate to the Map view of nearby grocery stores and shows if stock includes items from the shopping cart


## Wireframes
[Add picture of your hand sketched wireframes in this section]
<img src="YOUR_WIREFRAME_IMAGE_URL" width=600>

### [BONUS] Digital Wireframes & Mockups

### [BONUS] Interactive Prototype

## Schema 
[This section will be completed in Unit 9]
### Models
[Add table of models]
### Networking
- Request to Open Food API (https://world.openfoodfacts.org/) in "Home" screen after receiving the image from the user
    - This will tell us what product the user has scanned and more health information about it

- Request to Open Food API again for the purpose of allowing users to add items to their shopping list through search/filtering items within the Open Food database

- Request to Walmart, Walgreens or Google places API to get the closest grocery stores within the Map view 
    - 
- [Create basic snippets for each Parse network request]
- [OPTIONAL: List endpoints if using existing API such as Yelp]
