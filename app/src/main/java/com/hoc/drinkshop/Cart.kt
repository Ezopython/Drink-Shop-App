package com.hoc.drinkshop

import android.app.Application
import android.arch.persistence.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import org.koin.dsl.module.module


@Entity(tableName = "carts")
data class Cart(
        val name: String,
        val drinkId: Int,
        val number: Int,
        val comment: String,
        val cupSize: String,
        val sugar: Int,
        val ice: Int,
        val price: Double,
        @PrimaryKey(autoGenerate = true) var id: Int = 0
)

@Dao
interface CartDao {
    @Query("SELECT * FROM carts")
    fun getAllCart(): Flowable<List<Cart>>

    @Query("SELECT * FROM carts WHERE id=:id LIMIT 1")
    fun getCartById(id: Int): Flowable<Cart>

    @Query("SELECT COUNT(*) FROM carts")
    fun getCountCart(): Flowable<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCart(vararg cart: Cart)

    @Delete
    fun deleteCart(vararg cart: Cart)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateCart(vararg cart: Cart)

    @Query("DELETE FROM carts")
    fun deleteAllCart()
}

@Database(entities = [Cart::class], version = 1, exportSchema = false)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao

    companion object {
        const val CART_DB_NAME = "LocalDatabase"
    }
}

interface CartDataSource {
    fun getAllCart(): Flowable<List<Cart>>

    fun getCartById(id: Int): Flowable<Cart>

    fun getCountCart(): Flowable<Int>

    fun insertCart(vararg cart: Cart): Completable

    fun deleteCart(vararg cart: Cart): Completable

    fun updateCart(vararg cart: Cart): Completable

    fun deleteAllCart(): Completable
}

class CartRepository(private val dao: CartDao) : CartDataSource {
    override fun getAllCart(): Flowable<List<Cart>> = dao.getAllCart()

    override fun getCartById(id: Int): Flowable<Cart> = dao.getCartById(id)

    override fun getCountCart(): Flowable<Int> = dao.getCountCart()

    override fun insertCart(vararg cart: Cart): Completable {
        return Completable.fromAction {
            dao.insertCart(*cart)
        }
    }

    override fun deleteCart(vararg cart: Cart): Completable {
        return Completable.fromAction {
            dao.deleteCart(*cart)
        }
    }

    override fun updateCart(vararg cart: Cart): Completable {
        return Completable.fromAction {
            dao.updateCart(*cart)
        }
    }

    override fun deleteAllCart(): Completable {
        return Completable.fromAction {
            dao.deleteAllCart()
        }
    }

}

val cartModule = module {
    single {
        Room.databaseBuilder(get<Application>(), LocalDatabase::class.java, LocalDatabase.CART_DB_NAME)
                .build()
    }

    single { get<LocalDatabase>().cartDao() }

    single { CartRepository(get()) } bind CartDataSource::class
}