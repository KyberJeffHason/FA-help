package lotr.common;

import java.util.ArrayList;
import java.util.UUID;

public class LOTRPatron {
    private static String[] pledge5To10 = new String[] {"39f014d6-c028-4783-8c28-bef2b72b1cc7", "ef35a72a-ef00-4c2a-a2a9-58a54a7bb9fd", "c75394f3-6c1f-4efa-8568-2d02ece24ac9", "ae1b0559-b38b-4fd1-8c0c-7dc9afb8814b", "7e674e49-8a7e-4798-a6a1-5f16a8db6489", "db4bbf4c-5cd2-4929-95da-ce15256509d3", "5a72bd8f-83cf-43dc-a497-6ad48cf57443", "56097783-03b0-40a9-be4c-1746d2576cc8", "7d91a373-4b66-4d70-87fc-a4f37b0fa2ed", "ea900a96-99bb-4e5c-97e8-0a0caeef0a79", "8643b5d4-b0e8-4edc-beec-c83b2676a911", "b60c478a-01d5-4c7a-aaec-71de404585dc", "4f61d6e6-e688-49cd-9356-2319271d1bef", "22aa3a08-d8a5-4f13-a471-e29154a7eb83", "729dd43b-0ae7-4a27-acc4-1e66a55003ff", "a908bfd1-39f8-4735-8026-1526ac2e759c", "cc5b8640-0b8f-40a6-85d5-d7f51771c945", "d5da7dbe-5cdd-49c2-aa42-245358ba3f6c", "5ba78e5f-e705-4bda-ba27-4cfd837f0b3a"};
    private static String[] pledge10OrMore = new String[] {"7bc56da6-f133-4e47-8d0f-a2776762bca6", "e48a5ae5-623b-413b-90fe-f32a215e2dd6", "9d8b2b0b-43c4-4abd-9d07-eee7e4dc2d19", "fee47fb3-bf03-475b-91ff-0f80a103a770", "db4bbf4c-5cd2-4929-95da-ce15256509d3", "7b3e16ef-717e-40d7-9889-d510e5f6dffe", "65c8a2ad-5913-48b4-9985-a335cadb35d4"};
    public static UUID elfBowPlayer = UUID.fromString("e48a5ae5-623b-413b-90fe-f32a215e2dd6");

    public static String[] getShieldPlayers() {
        ArrayList<String> list = new ArrayList<String>();
        for(String s : pledge5To10) {
            list.add(s);
        }
        for(String s : pledge10OrMore) {
            list.add(s);
        }
        return list.toArray(new String[0]);
    }

    public static UUID[] getTitlePlayers() {
        ArrayList<UUID> list = new ArrayList<UUID>();
        for(String s : pledge5To10) {
            list.add(UUID.fromString(s));
        }
        for(String s : pledge10OrMore) {
            list.add(UUID.fromString(s));
        }
        return list.toArray(new UUID[0]);
    }
}
