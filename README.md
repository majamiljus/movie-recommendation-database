# Movie Recommendation Database

A Java and Microsoft SQL Server project for managing movies, users, ratings, watchlists, personalized recommendations, movie trends, user rewards, and thematic specialization.

The project focuses on relational database design and database-side business logic. Important system rules are enforced directly in Microsoft SQL Server using constraints, triggers, and stored procedures.

## Tech Stack

- Java
- Microsoft SQL Server
- SQL
- Relational Database Design
- Triggers
- Stored Procedures

## Overview

The system manages users, movies, genres, tags, ratings, and personal watchlists.

Each user has a unique username and a number of earned rewards. Movies contain a title and director and may belong to one or more genres and have multiple thematic tags.

Users can:

- Rate movies from 1 to 10
- Add movies to a personal watchlist
- Receive personalized movie recommendations
- Earn rewards
- Develop thematic specializations
- Receive a profile classification based on the diversity of their movie ratings

The system also automatically tracks movie popularity trends based on rating activity.

---

## Main Entities

The database contains entities for:

- Users
- Movies
- Genres
- Tags
- Ratings
- Watchlists
- Movie-genre relationships
- Movie-tag relationships
- User specializations

A movie can belong to multiple genres and can have multiple tags.

A user can rate multiple movies and maintain a personal list of movies they plan to watch.

---

## Movie Ratings

Users can rate movies with integer values from:

```text
1 - 10
```

The system includes a mechanism that prevents repeated abusive use of extreme ratings.

### Extreme Rating Protection

Ratings of:

```text
1
10
```

are considered extreme.

A user cannot continue assigning extreme ratings within a genre if they already have:

- More than 3 extreme ratings in that genre
- Fewer than 3 neutral ratings in that genre

Neutral ratings are:

```text
6
7
8
```

When the restriction becomes active, the user can still:

- Submit neutral ratings
- Change a previous extreme rating into a non-extreme rating

This allows the user to improve their rating behavior and eventually continue using the full rating range.

The rule is implemented through SQL Server triggers using the prefix:

```text
TR_BLOCK_EXTREME
```

---

## Personalized Movie Recommendations

The system generates movie recommendations based on each user's favorite genres.

### Favorite Genres

A genre is considered one of the user's favorite genres when the average rating given by that user to movies in the genre is:

```text
>= 8.0
```

### Recommendation Eligibility

A movie can be recommended when:

- It belongs to one of the user's favorite genres
- The user has not already rated it
- The movie is not already in the user's watchlist

The movie must also satisfy one of the following conditions.

### Regular Recommendation

The movie has:

```text
At least 4 ratings
Average rating >= 7.5
```

### Hidden Gem

A movie is considered a hidden gem when it has:

```text
Fewer than 4 ratings
Average rating >= 9.0
```

### Recommendation Order

Recommended movies are ordered by:

1. Higher average rating
2. Lower movie ID when two movies have the same average rating

Movie trend information is also available when recommendations are displayed.

---

## Movie Trend Tracking

The system automatically tracks movie popularity based on ratings and recent user activity.

Each movie can have one of the following statuses:

```text
Trending
Rising
Falling
Classic
```

The trend status is recalculated automatically whenever a movie rating is:

- Added
- Updated
- Deleted

This functionality is implemented using triggers with the prefix:

```text
TR_UPDATE_MOVIE_TREND
```

### Trending

A movie receives the `Trending` status when the number of new ratings it received during the last 30 days is among the highest in the system.

### Rising

A movie receives the `Rising` status when:

```text
average of its latest 5 ratings
>=
overall average rating + 1
```

This indicates that recent user reception is significantly better than the movie's historical average.

### Falling

A movie receives the `Falling` status when:

```text
average of its latest 5 ratings
<=
overall average rating - 1
```

This indicates that recent ratings are significantly worse than the movie's historical average.

### Classic

A movie receives the `Classic` status when it has:

```text
At least 3 ratings
Overall average rating >= 8.0
```

---

## User Reward System

Users can earn rewards for rating lower-rated movies inside their favorite genres.

A reward can be granted when:

- The movie belongs to one of the user's favorite genres
- The global average rating of that movie, excluding the user's own rating, is below `6`
- The user has rated enough movies to be eligible for rewards

A user can receive their first reward starting from their:

```text
10th rated movie
```

The reward logic is implemented using stored procedures with the prefix:

```text
SP_REWARD_USER_
```

The number of rewards earned by each user is stored in the system.

---

## Thematic Specialization

The system determines thematic specializations for users based on the tags of highly rated movies.

A user becomes specialized in a specific tag when that tag appears:

```text
At least 2 times
```

among movies that the user rated:

```text
>= 8
```

For example, if a user highly rates several movies tagged with:

```text
Space
```

the system can identify that user as specialized in the `Space` theme.

A user can have multiple thematic specializations.

---

## User Profile Classification

Users are also classified according to the diversity of movies they have rated.

Three classifications are supported:

```text
Curious
Focused
Undefined
```

### Curious

A user is classified as `Curious` when:

- They have rated at least 10 movies
- Those movies cover at least 10 different tags

```text
rated movies >= 10
distinct tags >= 10
```

### Focused

A user is classified as `Focused` when:

- They have rated at least 10 movies
- Those movies cover fewer than 10 different tags

```text
rated movies >= 10
distinct tags < 10
```

### Undefined

A user is classified as `Undefined` when they have rated fewer than 10 movies.

```text
rated movies < 10
```

---

## Watchlists

Each user can maintain a personal watchlist containing movies they want to watch.

The watchlist is also used by the recommendation system.

A movie is excluded from recommendations when it is already:

- Rated by the user
- Present in the user's watchlist

This prevents the recommendation system from suggesting movies that the user has already interacted with.

---

## Database Business Logic

An important part of the project is that critical business rules are implemented directly at the database level.

This includes:

### Extreme Rating Protection

```text
TR_BLOCK_EXTREME...
```

Responsible for preventing excessive extreme ratings within the same genre.

### Movie Trend Updates

```text
TR_UPDATE_MOVIE_TREND...
```

Responsible for automatically updating movie popularity status after rating changes.

### User Rewards

```text
SP_REWARD_USER_...
```

Responsible for checking reward conditions and updating user rewards.

This approach ensures that the rules remain enforced regardless of which application or client accesses the database.

---

## Referential Integrity

Where appropriate, relationships use:

```sql
ON UPDATE CASCADE
ON DELETE NO ACTION
```

This prevents the automatic deletion of related data when referenced records are removed.

Dependent records should normally be explicitly removed before deleting the record they reference.

Where explicit deletion is not practical, cascading deletion can be used when appropriate.

---

## Database Conventions

Primary-key ID columns that are not also foreign keys use SQL Server:

```sql
IDENTITY
```

Decimal values use:

```sql
DECIMAL(10,3)
```

The default maximum length for textual columns is:

```text
100 characters
```

unless a different size is specifically required.

---

## Example Recommendation Flow

```text
User
 │
 ├── Ratings
 │
 ▼
Calculate favorite genres
 │
 ▼
Average user rating per genre >= 8?
 │
 ├── No ──> Genre excluded
 │
 │
 └── Yes
      │
      ▼
 Find movies from favorite genre
      │
      ├── Already rated? ──> Exclude
      │
      ├── In watchlist? ──> Exclude
      │
      ▼
 Evaluate global ratings
      │
      ├── 4+ ratings and avg >= 7.5
      │
      └── <4 ratings and avg >= 9.0
      │
      ▼
 Add to recommendations
      │
      ▼
 Sort by average rating DESC
      │
      ▼
 Sort equal ratings by movie ID ASC
```

---

## Example Trend Flow

Whenever a rating is inserted, updated, or deleted:

```text
Rating changed
     │
     ▼
TR_UPDATE_MOVIE_TREND
     │
     ├── Check ratings from last 30 days
     │
     ├── Compare latest 5 ratings with overall average
     │
     ├── Check total number of ratings
     │
     ▼
Determine movie status
     │
     ├── Trending
     ├── Rising
     ├── Falling
     └── Classic
```

---

## Testing

The implementation is designed around predefined Java interfaces that expose the required database operations.

The Java implementation communicates with Microsoft SQL Server and is intended to support automated tests covering:

- User management
- Movie management
- Genres and tags
- Ratings
- Watchlists
- Recommendations
- Extreme-rating restrictions
- Movie trend calculation
- Rewards
- User specialization
- User profile classification

The database itself also enforces critical rules independently of the Java application layer.

---

## Suggested Project Structure

```text
movie-recommendation-database/
│
├── src/
│   └── ...
│
├── sql/
│   ├── schema.sql
│   ├── triggers.sql
│   ├── procedures.sql
│   └── data.sql
│
├── test/
│   └── ...
│
├── docs/
│   └── er-diagram.png
│
├── README.md
└── .gitignore
```

The exact structure may vary depending on how the Java and SQL files are organized in the project.

---

## Database Schema

If an ER diagram is included in the repository, it can be displayed here:

```md
![Database ER Diagram](docs/er-diagram.png)
```

The schema models the relationships between users, movies, genres, tags, ratings, watchlists, and user specializations.

---

## Project Type

Academic database systems project developed for the School of Electrical Engineering, University of Belgrade.
