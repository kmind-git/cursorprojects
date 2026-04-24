package com.example.authz.service;

import com.example.authz.domain.MenuEntity;
import com.example.authz.repo.MenuRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class MenuService {
  private final MenuRepository menuRepository;

  public MenuService(MenuRepository menuRepository) {
    this.menuRepository = menuRepository;
  }

  public List<MenuNode> getMenuTreeForPermissions(Set<String> permissionCodes) {
    List<MenuEntity> all = menuRepository.findAllByOrderBySortAscIdAsc();

    Map<Long, MenuNode> idToNode = new HashMap<>();
    for (MenuEntity m : all) {
      if (!m.isVisible()) {
        continue;
      }
      if (!hasMenuAccess(m, permissionCodes)) {
        continue;
      }
      idToNode.put(m.getId(), toNode(m));
    }

    List<MenuNode> roots = new ArrayList<>();
    for (MenuEntity m : all) {
      MenuNode node = idToNode.get(m.getId());
      if (node == null) continue;

      MenuEntity parent = m.getParent();
      if (parent == null || !idToNode.containsKey(parent.getId())) {
        roots.add(node);
      } else {
        idToNode.get(parent.getId()).children.add(node);
      }
    }

    return roots;
  }

  private boolean hasMenuAccess(MenuEntity menu, Set<String> permissionCodes) {
    if (menu.getPermissions() == null || menu.getPermissions().isEmpty()) {
      return true;
    }
    Set<String> required = new HashSet<>();
    menu.getPermissions().forEach(p -> required.add(p.getCode()));
    return permissionCodes.containsAll(required);
  }

  private MenuNode toNode(MenuEntity m) {
    MenuNode n = new MenuNode();
    n.id = m.getId();
    n.name = m.getName();
    n.path = m.getPath();
    n.component = m.getComponent();
    n.icon = m.getIcon();
    n.sort = m.getSort();
    return n;
  }

  public static final class MenuNode {
    public Long id;
    public String name;
    public String path;
    public String component;
    public String icon;
    public int sort;
    public List<MenuNode> children = new ArrayList<>();
  }
}

