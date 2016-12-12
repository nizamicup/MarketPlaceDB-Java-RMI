package Bank;

import java.io.Serializable;

/**
 * Specifies a read-only view of n account.
 */
public interface AccountDTO extends Serializable {
    /**
     * @return The balance.
     */
    public int getBalance();

    /**
     * @return The holder's name.
     */
    public String getHolderName();
}
