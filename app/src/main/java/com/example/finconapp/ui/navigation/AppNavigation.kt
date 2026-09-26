package com.example.finconapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.finconapp.ui.screens.AddTransactionSheet
import com.example.finconapp.ui.screens.CategoryScreen
import com.example.finconapp.ui.screens.HomeScreen
import com.example.finconapp.ui.screens.SavingsScreen
import com.example.finconapp.ui.screens.TransactionScreen
import com.example.finconapp.ui.viewmodel.TransactionViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.example.finconapp.data.local.database.DatabaseProvider
import com.example.finconapp.data.local.entity.Category
import com.example.finconapp.data.repository.CategoryRepository
import com.example.finconapp.ui.viewmodel.CategoryViewModel
import com.example.finconapp.ui.viewmodel.CategoryViewModelFactory
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

sealed class Screen(val route: String) {

    data object Home : Screen("home")

    data object Transactions : Screen("transactions")

    data object Categories : Screen("categories")

    data object Savings : Screen("savings")
}


@Composable
fun AppNavigation(
    navController: NavHostController
) {

    val currentBackStackEntry =
        navController.currentBackStackEntryAsState()

    val currentRoute =
        currentBackStackEntry.value?.destination?.route


    val navigationItems = listOf(
        Screen.Home,
        Screen.Transactions,
        Screen.Categories,
        Screen.Savings
    )


    var showAddSheet by remember {
        mutableStateOf(false)
    }

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    val database = remember { DatabaseProvider.provide(context) }

    val categoryRepository = remember { CategoryRepository( database.categoryDao() ) }

    val transactionViewModel: TransactionViewModel = viewModel()
    val categoryViewModel: CategoryViewModel = viewModel( factory = CategoryViewModelFactory( categoryRepository ) )

    val categories by categoryViewModel.categories.collectAsState()

    val showFloatingActionButton =
        currentRoute == Screen.Home.route ||
                currentRoute == Screen.Transactions.route ||
                currentRoute == Screen.Categories.route


    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        },
        floatingActionButton = {

            if (showFloatingActionButton) {

                FloatingActionButton(
                    onClick = {
                        showAddSheet = true
                    }
                ) {

                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar"
                    )
                }
            }
        },


        bottomBar = {

            NavigationBar {

                navigationItems.forEach { screen ->

                    NavigationBarItem(

                        selected =
                            currentRoute == screen.route,

                        onClick = {

                            navController.navigate(
                                screen.route
                            ) {

                                popUpTo(
                                    Screen.Home.route
                                ) {
                                    saveState = true
                                }

                                launchSingleTop = true

                                restoreState = true
                            }
                        },


                        icon = {

                            when (screen) {

                                Screen.Home -> {

                                    Icon(
                                        imageVector =
                                            Icons.Default.Home,
                                        contentDescription =
                                            "Home"
                                    )
                                }

                                Screen.Transactions -> {

                                    Icon(
                                        imageVector =
                                            Icons.AutoMirrored.Filled.List,
                                        contentDescription =
                                            "Movimientos"
                                    )
                                }

                                Screen.Categories -> {

                                    Icon(
                                        imageVector =
                                            Icons.Default.Category,
                                        contentDescription =
                                            "Categorías"
                                    )
                                }

                                Screen.Savings -> {

                                    Icon(
                                        imageVector =
                                            Icons.Default.AccountBalance,
                                        contentDescription =
                                            "Ahorros"
                                    )
                                }
                            }
                        },


                        label = {

                            Text(
                                text = when (screen) {

                                    Screen.Home ->
                                        "Home"

                                    Screen.Transactions ->
                                        "Movimientos"

                                    Screen.Categories ->
                                        "Categorías"

                                    Screen.Savings ->
                                        "Ahorros"
                                }
                            )
                        }
                    )
                }
            }
        }

    ) { paddingValues ->


        NavHost(
            navController = navController,
            startDestination = Screen.Home.route
        ) {


            composable(
                Screen.Home.route
            ) {

                HomeScreen(
                    paddingValues = paddingValues,
                    viewModel = transactionViewModel,
                    categoriesList = categories
                )
            }


            composable(
                Screen.Transactions.route
            ) {

                TransactionScreen(
                    paddingValues = paddingValues,
                    viewModel = transactionViewModel,
                    categoriesList = categories,
                    onCreateCategory = { categoryName, onCategoryCreated ->

                        categoryViewModel.insert(
                            Category(
                                name = categoryName.trim()
                            )
                        ) { success, categoryId ->

                            if (!success) {

                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "Esta categoría ya existe"
                                    )
                                }

                                onCategoryCreated(null)

                            } else {

                                onCategoryCreated(categoryId)
                            }
                        }
                    }
                )
            }


            composable(
                Screen.Categories.route
            ) {

                CategoryScreen(
                    paddingValues = paddingValues,
                    viewModel = transactionViewModel,
                    categoryViewModel = categoryViewModel,
                    categoriesList = categories
                )
            }


            composable(
                Screen.Savings.route
            ) {

                SavingsScreen(
                    paddingValues = paddingValues
                )
            }
        }
    }


    if (showAddSheet) {

        AddTransactionSheet(

            categories = categories,

            onDismiss = {
                showAddSheet = false
            },

            onSave = { transaction ->

                transactionViewModel.insert(
                    transaction
                )

                showAddSheet = false
            },

            onCreateCategory = { categoryName, onCategoryCreated ->

                categoryViewModel.insert(
                    Category(
                        name = categoryName.trim()
                    )
                ) { success, categoryId ->

                    if (!success) {

                        // Mostrar:
                        // "Ya existe una categoría con ese nombre"

                        onCategoryCreated(null)

                    } else {

                        onCategoryCreated(categoryId)
                    }
                }
            }
        )
    }
}